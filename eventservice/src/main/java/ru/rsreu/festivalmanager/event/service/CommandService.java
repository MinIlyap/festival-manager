package ru.rsreu.festivalmanager.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.event.dto.CommandInviteEvent;
import ru.rsreu.festivalmanager.event.entity.registration.*;
import ru.rsreu.festivalmanager.event.entity.event.FestivalEvent;
import ru.rsreu.festivalmanager.event.entity.registration.Command;
import ru.rsreu.festivalmanager.event.entity.registration.CommandMember;
import ru.rsreu.festivalmanager.event.entity.registration.CommandMemberId;
import ru.rsreu.festivalmanager.event.entity.registration.UserEventRegistrationId;
import ru.rsreu.festivalmanager.event.kafka.CommandKafkaProducer;
import ru.rsreu.festivalmanager.event.repository.*;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CommandService {

    private final CommandRepository commandRepository;
    private final CommandMemberRepository commandMemberRepository;
    private final FestivalEventRepository festivalEventRepository;
    private final UserEventRegistrationRepository registrationRepository;
    private final CommandKafkaProducer kafkaProducer;

    @Transactional
    public Long createCommand(Long festivalEventId, String commandName, Long creatorId) {
        FestivalEvent festivalEvent = festivalEventRepository.findById(festivalEventId)
                .orElseThrow(() -> new IllegalArgumentException("Мероприятие не найдено"));

        if (!festivalEvent.getEventInstance().getIsCommand()) {
            throw new IllegalStateException("Это мероприятие не является командным");
        }

        // 1. Проверяем, зарегистрирован ли создатель на само мероприятие
        // (Используем твой объектный составной ключ!)
        UserEventRegistrationId regId = new UserEventRegistrationId(festivalEvent, creatorId);
        if (!registrationRepository.existsById(regId)) {
            throw new IllegalStateException("Вы должны быть зарегистрированы на мероприятие, чтобы создать команду");
        }

        // 2. Проверяем, не состоит ли он уже в другой команде на этом мероприятии
        if (commandMemberRepository.existsByCommandMemberId_UserIdAndCommandMemberId_Command_FestivalEvent_id(creatorId, festivalEventId)) {
            throw new IllegalStateException("Вы уже состоите в команде на этом мероприятии");
        }

        // 3. Создаем команду
        Command command = Command.builder()
                .festivalEvent(festivalEvent)
                .name(commandName)
                .creatorId(creatorId)
                .build();
        command = commandRepository.save(command);

        // 4. Добавляем создателя как первого участника
        CommandMember member = new CommandMember(new CommandMemberId(command, creatorId));
        commandMemberRepository.save(member);

        return command.getId();
    }

    @Transactional(readOnly = true)
    public void inviteUserToCommand(Long commandId, Long targetUserId, Long senderId) {
        Command command = commandRepository.findById(commandId)
                .orElseThrow(() -> new IllegalArgumentException("Команда не найдена"));

        // Проверяем, имеет ли право отправитель приглашать (он должен быть участником команды)
        if (!commandMemberRepository.existsByCommandMemberId_UserIdAndCommandMemberId_Command_Id(senderId, commandId)) {
            throw new IllegalStateException("Вы не состоите в этой команде и не можете рассылать приглашения");
        }

        // Проверяем, зарегистрирован ли приглашаемый на мероприятие
        UserEventRegistrationId regId = new UserEventRegistrationId(command.getFestivalEvent(), targetUserId);
        if (!registrationRepository.existsById(regId)) {
            throw new IllegalStateException("Пользователь еще не зарегистрирован на это мероприятие");
        }

        // Проверяем, не в команде ли он уже
        if (commandMemberRepository.existsByCommandMemberId_UserIdAndCommandMemberId_Command_FestivalEvent_id(targetUserId, command.getFestivalEvent().getId())) {
            throw new IllegalStateException("Пользователь уже состоит в другой команде на этом мероприятии");
        }

        // Отправляем в Kafka
        CommandInviteEvent event = CommandInviteEvent.builder()
                .festivalEventId(command.getFestivalEvent().getId())
                .eventName(command.getFestivalEvent().getEventInstance().getEvent().getName())
                .commandId(command.getId())
                .commandName(command.getName())
                .relatedUser(targetUserId)
                .senderId(senderId)
                .createdAt(OffsetDateTime.now())
                .build();

        kafkaProducer.sendInvite(event);
    }

    @Transactional
    public void joinCommand(Long commandId, Long userId) {
        // Блокируем команду от гонок данных, чтобы не превысить лимит
        Command command = commandRepository.findByIdForUpdate(commandId)
                .orElseThrow(() -> new IllegalArgumentException("Команда не найдена"));

        // Проверяем лимит
        Integer maxPeople = command.getFestivalEvent().getEventInstance().getMaxPeopleCommand();
        long currentMembers = commandMemberRepository.countByCommandMemberId_Command_id(commandId);

        if (maxPeople != null && currentMembers >= maxPeople) {
            throw new IllegalStateException("Команда полностью укомплектована");
        }

        // Двойная защита: вдруг он уже принял другое приглашение, пока это висело
        if (commandMemberRepository.existsByCommandMemberId_UserIdAndCommandMemberId_Command_FestivalEvent_id(userId, command.getFestivalEvent().getId())) {
            throw new IllegalStateException("Вы уже состоите в команде на этом мероприятии");
        }

        // Добавляем участника
        CommandMember member = new CommandMember(new CommandMemberId(command, userId));
        commandMemberRepository.save(member);
    }

    @Transactional
    public void disbandCommand(Long commandId, Long userId) {
        Command command = commandRepository.findById(commandId)
                .orElseThrow(() -> new IllegalArgumentException("Команда не найдена"));

        if (!command.getCreatorId().equals(userId)) {
            throw new IllegalStateException("Только создатель может расформировать команду");
        }

        commandRepository.delete(command);
    }
}