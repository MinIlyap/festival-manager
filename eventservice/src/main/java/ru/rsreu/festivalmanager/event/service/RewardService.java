package ru.rsreu.festivalmanager.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.event.dto.EventRewardResponse;
import ru.rsreu.festivalmanager.event.dto.RewardAddRequest;
import ru.rsreu.festivalmanager.event.entity.event.FestivalEvent;
import ru.rsreu.festivalmanager.event.entity.registration.CommandMember;
import ru.rsreu.festivalmanager.event.entity.registration.UserEventRegistrationId;
import ru.rsreu.festivalmanager.event.entity.reward.*;
import ru.rsreu.festivalmanager.event.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final FestivalEventRepository festivalEventRepository;
    private final RewardRepository rewardRepository;
    private final RewardInstanceRepository rewardInstanceRepository;
    private final EventRewardRepository eventRewardRepository;
    private final RewardDistributionRepository rewardDistributionRepository;
    private final UserEventRegistrationRepository registrationRepository;
    private final CommandMemberRepository commandMemberRepository;

    @Transactional(readOnly = true)
    public List<EventRewardResponse> getRewardsByEvent(Long festivalEventId) {
        return eventRewardRepository.findAllByFestivalEvent_Id(festivalEventId)
                .stream()
                .map(er -> EventRewardResponse.builder()
                        .eventRewardId(er.getId())
                        .rewardId(er.getRewardInstance().getReward().getId())
                        .rewardName(er.getRewardInstance().getReward().getName())
                        .description(er.getRewardInstance().getDescription())
                        .place(er.getRewardInstance().getPlace())
                        .pointNumber(er.getRewardInstance().getPointNumber())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeRewardFromEvent(Long eventRewardId, Long companyId) {
        EventReward eventReward = eventRewardRepository.findById(eventRewardId)
                .orElseThrow(() -> new IllegalArgumentException("Награда не найдена"));

        if (!eventReward.getFestivalEvent().getEventInstance().getCompanyId().equals(companyId)) {
            throw new IllegalStateException("У вас нет прав на удаление этой награды");
        }

        eventRewardRepository.delete(eventReward);
    }

    /**
     * Создание награды и привязка к мероприятию
     */
    @Transactional
    public Long addRewardToEvent(Long festivalEventId, Long companyId, RewardAddRequest request) {
        FestivalEvent festivalEvent = festivalEventRepository.findById(festivalEventId)
                .orElseThrow(() -> new IllegalArgumentException("Мероприятие не найдено"));

        // Проверяем, что награду добавляет владелец мероприятия
        if (!festivalEvent.getEventInstance().getCompanyId().equals(companyId)) {
            throw new IllegalStateException("У вас нет прав на редактирование этого мероприятия");
        }

        // 1. Ищем базовую награду в словаре
        Reward baseReward = rewardRepository.findById(request.getRewardId())
                .orElseThrow(() -> new IllegalArgumentException("Базовая награда не найдена"));

        // 2. Ищем точную копию инстанса награды ИЛИ создаем новую
        RewardInstance rewardInstance = rewardInstanceRepository.findIdenticalInstance(
                request.getRewardId(),
                request.getDescription(),
                request.getPlace(),
                request.getPointNumber()
        ).orElseGet(() -> {
            RewardInstance newInstance = RewardInstance.builder()
                    .reward(baseReward)
                    .description(request.getDescription())
                    .place(request.getPlace())
                    .pointNumber(request.getPointNumber())
                    .build();
            return rewardInstanceRepository.save(newInstance);
        });

        // 3. Привязываем к мероприятию
        EventReward eventReward = EventReward.builder()
                .festivalEvent(festivalEvent)
                .rewardInstance(rewardInstance)
                .build();

        return eventRewardRepository.save(eventReward).getId();
    }

    /**
     * Выдача награды конкретным пользователям (массово)
     */
    @Transactional
    public void distributeRewardToUsers(Long eventRewardId, Long companyId, List<Long> userIds) {
        EventReward eventReward = eventRewardRepository.findById(eventRewardId)
                .orElseThrow(() -> new IllegalArgumentException("Награда не найдена"));

        FestivalEvent festivalEvent = eventReward.getFestivalEvent();

        if (!festivalEvent.getEventInstance().getCompanyId().equals(companyId)) {
            throw new IllegalStateException("У вас нет прав на распределение наград этого мероприятия");
        }

        for (Long userId : userIds) {
            // 1. Проверяем, зарегистрирован ли пользователь на мероприятие
            UserEventRegistrationId regId = new UserEventRegistrationId(festivalEvent, userId);
            if (!registrationRepository.existsById(regId)) {
                throw new IllegalStateException("Пользователь с ID " + userId + " не зарегистрирован на это мероприятие");
            }

            // 2. Проверяем, не выдана ли эта награда ему ранее
            if (rewardDistributionRepository.existsByRewardDistributionId_EventReward_IdAndRewardDistributionId_UserId(eventRewardId, userId)) {
                continue; // Пропускаем, чтобы не дублировать (или можно кидать ошибку)
            }

            // 3. Выдаем награду
            RewardDistribution distribution = new RewardDistribution();
            distribution.setRewardDistributionId(new RewardDistributionId(eventReward, userId));
            rewardDistributionRepository.save(distribution);
        }
    }

    /**
     * Умная выдача награды целой команде (для командных мероприятий)
     */
    @Transactional
    public void distributeRewardToCommand(Long eventRewardId, Long companyId, Long commandId) {
        EventReward eventReward = eventRewardRepository.findById(eventRewardId)
                .orElseThrow(() -> new IllegalArgumentException("Награда не найдена"));

        if (!eventReward.getFestivalEvent().getEventInstance().getCompanyId().equals(companyId)) {
            throw new IllegalStateException("У вас нет прав на распределение наград");
        }

        // Ищем всех участников команды
        List<CommandMember> members = commandMemberRepository.findAllByCommandMemberId_Command_Id(commandId);

        for (CommandMember member : members) {
            Long userId = member.getCommandMemberId().getUserId();

            // Проверяем, не выдана ли эта награда ему ранее
            if (!rewardDistributionRepository.existsByRewardDistributionId_EventReward_IdAndRewardDistributionId_UserId(eventRewardId, userId)) {
                RewardDistribution distribution = new RewardDistribution();
                distribution.setRewardDistributionId(new RewardDistributionId(eventReward, userId));
                rewardDistributionRepository.save(distribution);
            }
        }
    }

    @Transactional
    public void revokeRewardFromUser(Long eventRewardId, Long companyId, Long userId) {
        EventReward eventReward = eventRewardRepository.findById(eventRewardId)
                .orElseThrow(() -> new IllegalArgumentException("Награда не найдена"));

        if (!eventReward.getFestivalEvent().getEventInstance().getCompanyId().equals(companyId)) {
            throw new IllegalStateException("У вас нет прав на редактирование наград");
        }

        RewardDistributionId distId = new RewardDistributionId(eventReward, userId);
        rewardDistributionRepository.findById(distId).ifPresent(rewardDistributionRepository::delete);
    }

    @Transactional
    public void revokeRewardFromCommand(Long eventRewardId, Long companyId, Long commandId) {
        EventReward eventReward = eventRewardRepository.findById(eventRewardId)
                .orElseThrow(() -> new IllegalArgumentException("Награда не найдена"));

        if (!eventReward.getFestivalEvent().getEventInstance().getCompanyId().equals(companyId)) {
            throw new IllegalStateException("У вас нет прав на редактирование наград");
        }

        List<CommandMember> members = commandMemberRepository.findAllByCommandMemberId_Command_Id(commandId);
        for (CommandMember member : members) {
            Long userId = member.getCommandMemberId().getUserId();
            RewardDistributionId distId = new RewardDistributionId(eventReward, userId);

            // Удаляем раздачу, если она есть
            rewardDistributionRepository.findById(distId).ifPresent(rewardDistributionRepository::delete);
        }
    }
}
