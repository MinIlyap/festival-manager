package ru.rsreu.festivalmanager.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.event.dto.CommandWithMembersResponse;
import ru.rsreu.festivalmanager.event.entity.event.FestivalEvent;
import ru.rsreu.festivalmanager.event.entity.registration.Command;
import ru.rsreu.festivalmanager.event.repository.CommandRepository;
import ru.rsreu.festivalmanager.event.repository.FestivalEventRepository;
import ru.rsreu.festivalmanager.event.repository.UserEventRegistrationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventInfoService {

    private final UserEventRegistrationRepository registrationRepository;
    private final CommandRepository commandRepository;
    private final FestivalEventRepository festivalEventRepository;

    @Transactional(readOnly = true)
    public List<Long> getAllParticipants(Long festivalEventId) {
        return registrationRepository.findAllByUserEventRegistrationId_FestivalEvent_Id(festivalEventId)
                .stream()
                .map(reg -> reg.getUserEventRegistrationId().getUserId())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommandWithMembersResponse> getCommandsWithMembers(Long festivalEventId) {
        List<Command> commands = commandRepository.findAllByFestivalEvent_Id(festivalEventId);

        return commands.stream().map(cmd -> {
            List<Long> memberIds = cmd.getMembers().stream()
                    .map(member -> member.getCommandMemberId().getUserId())
                    .collect(Collectors.toList());

            return CommandWithMembersResponse.builder()
                    .commandId(cmd.getId())
                    .commandName(cmd.getName())
                    .creatorId(cmd.getCreatorId())
                    .memberUserIds(memberIds)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Long> getParticipantsWithoutCommand(Long festivalEventId) {
        FestivalEvent event = festivalEventRepository.findById(festivalEventId)
                .orElseThrow(() -> new IllegalArgumentException("Мероприятие не найдено"));

        if (!event.getEventInstance().getIsCommand()) {
            throw new IllegalStateException("Это мероприятие не является командным");
        }

        return registrationRepository.findUserIdsWithoutCommand(festivalEventId);
    }
}