package ru.rsreu.festivalmanager.event.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import ru.rsreu.festivalmanager.event.dto.event.CommandInviteEvent;
import ru.rsreu.festivalmanager.event.entity.event.Event;
import ru.rsreu.festivalmanager.event.entity.event.EventInstance;
import ru.rsreu.festivalmanager.event.entity.event.FestivalEvent;
import ru.rsreu.festivalmanager.event.entity.registration.Command;
import ru.rsreu.festivalmanager.event.entity.registration.CommandMember;
import ru.rsreu.festivalmanager.event.repository.CommandMemberRepository;
import ru.rsreu.festivalmanager.event.repository.CommandRepository;
import ru.rsreu.festivalmanager.event.repository.FestivalEventRepository;
import ru.rsreu.festivalmanager.event.repository.UserEventRegistrationRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommandServiceTest {

    private static final long EVENT_ID = 3L;
    private static final long COMMAND_ID = 9L;
    private static final long CREATOR_ID = 42L;
    private static final long OTHER_USER_ID = 77L;

    @Mock private CommandRepository commandRepository;
    @Mock private CommandMemberRepository commandMemberRepository;
    @Mock private FestivalEventRepository festivalEventRepository;
    @Mock private UserEventRegistrationRepository registrationRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private CommandService commandService;

    private static FestivalEvent festivalEvent(boolean isCommand, Integer maxPeopleCommand) {
        return FestivalEvent.builder()
                .id(EVENT_ID)
                .festivalLocationId(11L)
                .eventInstance(EventInstance.builder()
                        .id(1L)
                        .isCommand(isCommand)
                        .maxPeopleCommand(maxPeopleCommand)
                        .event(Event.builder().id(1L).name("Турнир").build())
                        .build())
                .build();
    }

    private static Command command(FestivalEvent event) {
        return Command.builder()
                .id(COMMAND_ID)
                .name("Альфа")
                .festivalEvent(event)
                .creatorId(CREATOR_ID)
                .build();
    }

    /** Регистрация участника на мероприятие. */
    private void registered(long userId, boolean value) {
        when(registrationRepository.existsById(
                argThatMatchesUser(userId))).thenReturn(value);
    }

    private static ru.rsreu.festivalmanager.event.entity.registration.UserEventRegistrationId argThatMatchesUser(long userId) {
        return org.mockito.ArgumentMatchers.argThat(id -> id != null && userId == id.getUserId());
    }

    @Nested
    @DisplayName("Создание команды")
    class CreateCommand {

        @Test
        @DisplayName("на некомандном мероприятии команду создать нельзя")
        void rejectsNonCommandEvent() {
            when(festivalEventRepository.findById(EVENT_ID))
                    .thenReturn(Optional.of(festivalEvent(false, 5)));

            assertThatThrownBy(() -> commandService.createCommand(EVENT_ID, "Альфа", CREATOR_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("не является командным");

            verify(commandRepository, never()).save(any());
        }

        @Test
        @DisplayName("без регистрации на мероприятие команду создать нельзя")
        void requiresRegistration() {
            when(festivalEventRepository.findById(EVENT_ID))
                    .thenReturn(Optional.of(festivalEvent(true, 5)));
            registered(CREATOR_ID, false);

            assertThatThrownBy(() -> commandService.createCommand(EVENT_ID, "Альфа", CREATOR_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("зарегистрированы");

            verify(commandRepository, never()).save(any());
        }

        @Test
        @DisplayName("нельзя состоять в двух командах одного мероприятия")
        void rejectsSecondCommandOnSameEvent() {
            when(festivalEventRepository.findById(EVENT_ID))
                    .thenReturn(Optional.of(festivalEvent(true, 5)));
            registered(CREATOR_ID, true);
            when(commandMemberRepository
                    .existsByCommandMemberId_UserIdAndCommandMemberId_Command_FestivalEvent_id(CREATOR_ID, EVENT_ID))
                    .thenReturn(true);

            assertThatThrownBy(() -> commandService.createCommand(EVENT_ID, "Альфа", CREATOR_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("уже состоите в команде");

            verify(commandRepository, never()).save(any());
        }

        @Test
        @DisplayName("создатель сразу становится участником своей команды")
        void addsCreatorAsMember() {
            FestivalEvent event = festivalEvent(true, 5);
            when(festivalEventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            registered(CREATOR_ID, true);
            when(commandMemberRepository
                    .existsByCommandMemberId_UserIdAndCommandMemberId_Command_FestivalEvent_id(CREATOR_ID, EVENT_ID))
                    .thenReturn(false);
            when(commandRepository.save(any(Command.class))).thenAnswer(invocation -> {
                Command saved = invocation.getArgument(0);
                saved.setId(COMMAND_ID);
                return saved;
            });

            Long id = commandService.createCommand(EVENT_ID, "Альфа", CREATOR_ID);

            assertThat(id).isEqualTo(COMMAND_ID);
            ArgumentCaptor<CommandMember> member = ArgumentCaptor.forClass(CommandMember.class);
            verify(commandMemberRepository).save(member.capture());
            assertThat(member.getValue().getCommandMemberId().getUserId()).isEqualTo(CREATOR_ID);
        }
    }

    @Nested
    @DisplayName("Приглашение в команду")
    class InviteUser {

        @Test
        @DisplayName("приглашать может только участник команды")
        void onlyMembersCanInvite() {
            when(commandRepository.findById(COMMAND_ID))
                    .thenReturn(Optional.of(command(festivalEvent(true, 5))));
            when(commandMemberRepository
                    .existsByCommandMemberId_UserIdAndCommandMemberId_Command_Id(OTHER_USER_ID, COMMAND_ID))
                    .thenReturn(false);

            assertThatThrownBy(() -> commandService.inviteUserToCommand(COMMAND_ID, 100L, OTHER_USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("не состоите в этой команде");

            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("нельзя пригласить незарегистрированного на мероприятие")
        void requiresTargetRegistration() {
            when(commandRepository.findById(COMMAND_ID))
                    .thenReturn(Optional.of(command(festivalEvent(true, 5))));
            when(commandMemberRepository
                    .existsByCommandMemberId_UserIdAndCommandMemberId_Command_Id(CREATOR_ID, COMMAND_ID))
                    .thenReturn(true);
            registered(100L, false);

            assertThatThrownBy(() -> commandService.inviteUserToCommand(COMMAND_ID, 100L, CREATOR_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("еще не зарегистрирован");

            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("в укомплектованную команду приглашения не уходят")
        void rejectsInviteWhenCommandIsFull() {
            when(commandRepository.findById(COMMAND_ID))
                    .thenReturn(Optional.of(command(festivalEvent(true, 3))));
            when(commandMemberRepository
                    .existsByCommandMemberId_UserIdAndCommandMemberId_Command_Id(CREATOR_ID, COMMAND_ID))
                    .thenReturn(true);
            registered(100L, true);
            when(commandMemberRepository
                    .existsByCommandMemberId_UserIdAndCommandMemberId_Command_FestivalEvent_id(100L, EVENT_ID))
                    .thenReturn(false);
            when(commandMemberRepository.countByCommandMemberId_Command_id(COMMAND_ID)).thenReturn(3L);

            assertThatThrownBy(() -> commandService.inviteUserToCommand(COMMAND_ID, 100L, CREATOR_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("укомплектована");

            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("успешное приглашение публикует событие с адресатом и командой")
        void publishesInviteEvent() {
            when(commandRepository.findById(COMMAND_ID))
                    .thenReturn(Optional.of(command(festivalEvent(true, 5))));
            when(commandMemberRepository
                    .existsByCommandMemberId_UserIdAndCommandMemberId_Command_Id(CREATOR_ID, COMMAND_ID))
                    .thenReturn(true);
            registered(100L, true);
            when(commandMemberRepository
                    .existsByCommandMemberId_UserIdAndCommandMemberId_Command_FestivalEvent_id(100L, EVENT_ID))
                    .thenReturn(false);
            when(commandMemberRepository.countByCommandMemberId_Command_id(COMMAND_ID)).thenReturn(1L);

            commandService.inviteUserToCommand(COMMAND_ID, 100L, CREATOR_ID);

            ArgumentCaptor<CommandInviteEvent> event = ArgumentCaptor.forClass(CommandInviteEvent.class);
            verify(eventPublisher).publishEvent(event.capture());
            assertThat(event.getValue().getRelatedUser()).isEqualTo(100L);
            assertThat(event.getValue().getSenderId()).isEqualTo(CREATOR_ID);
            assertThat(event.getValue().getCommandId()).isEqualTo(COMMAND_ID);
            assertThat(event.getValue().getCommandName()).isEqualTo("Альфа");
            assertThat(event.getValue().getEventName()).isEqualTo("Турнир");
        }

        @Test
        @DisplayName("без ограничения на размер команды приглашение проходит")
        void allowsInviteWhenLimitIsNotSet() {
            when(commandRepository.findById(COMMAND_ID))
                    .thenReturn(Optional.of(command(festivalEvent(true, null))));
            when(commandMemberRepository
                    .existsByCommandMemberId_UserIdAndCommandMemberId_Command_Id(CREATOR_ID, COMMAND_ID))
                    .thenReturn(true);
            registered(100L, true);
            when(commandMemberRepository
                    .existsByCommandMemberId_UserIdAndCommandMemberId_Command_FestivalEvent_id(100L, EVENT_ID))
                    .thenReturn(false);
            when(commandMemberRepository.countByCommandMemberId_Command_id(COMMAND_ID)).thenReturn(999L);

            commandService.inviteUserToCommand(COMMAND_ID, 100L, CREATOR_ID);

            verify(eventPublisher).publishEvent(any(CommandInviteEvent.class));
        }
    }

    @Nested
    @DisplayName("Вступление в команду")
    class JoinCommand {

        @Test
        @DisplayName("в полную команду вступить нельзя")
        void rejectsJoiningFullCommand() {
            when(commandRepository.findByIdForUpdate(COMMAND_ID))
                    .thenReturn(Optional.of(command(festivalEvent(true, 2))));
            registered(OTHER_USER_ID, true);
            when(commandMemberRepository.countByCommandMemberId_Command_id(COMMAND_ID)).thenReturn(2L);

            assertThatThrownBy(() -> commandService.joinCommand(COMMAND_ID, OTHER_USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("укомплектована");

            verify(commandMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("без регистрации на мероприятие вступить нельзя")
        void requiresRegistrationToJoin() {
            when(commandRepository.findByIdForUpdate(COMMAND_ID))
                    .thenReturn(Optional.of(command(festivalEvent(true, 5))));
            registered(OTHER_USER_ID, false);

            assertThatThrownBy(() -> commandService.joinCommand(COMMAND_ID, OTHER_USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("зарегистрированы");

            verify(commandMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("состоящий в другой команде на том же мероприятии не вступит")
        void rejectsUserAlreadyInAnotherCommand() {
            when(commandRepository.findByIdForUpdate(COMMAND_ID))
                    .thenReturn(Optional.of(command(festivalEvent(true, 5))));
            registered(OTHER_USER_ID, true);
            when(commandMemberRepository.countByCommandMemberId_Command_id(COMMAND_ID)).thenReturn(1L);
            when(commandMemberRepository
                    .existsByCommandMemberId_UserIdAndCommandMemberId_Command_FestivalEvent_id(OTHER_USER_ID, EVENT_ID))
                    .thenReturn(true);

            assertThatThrownBy(() -> commandService.joinCommand(COMMAND_ID, OTHER_USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("уже состоите в команде");

            verify(commandMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("при свободном месте участник добавляется")
        void addsMemberWhenThereIsRoom() {
            when(commandRepository.findByIdForUpdate(COMMAND_ID))
                    .thenReturn(Optional.of(command(festivalEvent(true, 5))));
            registered(OTHER_USER_ID, true);
            when(commandMemberRepository.countByCommandMemberId_Command_id(COMMAND_ID)).thenReturn(1L);
            when(commandMemberRepository
                    .existsByCommandMemberId_UserIdAndCommandMemberId_Command_FestivalEvent_id(OTHER_USER_ID, EVENT_ID))
                    .thenReturn(false);

            commandService.joinCommand(COMMAND_ID, OTHER_USER_ID);

            ArgumentCaptor<CommandMember> member = ArgumentCaptor.forClass(CommandMember.class);
            verify(commandMemberRepository).save(member.capture());
            assertThat(member.getValue().getCommandMemberId().getUserId()).isEqualTo(OTHER_USER_ID);
        }
    }

    @Nested
    @DisplayName("Выход и расформирование")
    class LeaveAndDisband {

        @Test
        @DisplayName("создатель не может просто выйти из своей команды")
        void creatorCannotLeave() {
            when(commandRepository.findById(COMMAND_ID))
                    .thenReturn(Optional.of(command(festivalEvent(true, 5))));

            assertThatThrownBy(() -> commandService.leaveCommand(COMMAND_ID, CREATOR_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("расформируйте");

            verify(commandMemberRepository, never())
                    .deleteByCommandMemberId_UserIdAndCommandMemberId_Command_Id(any(), any());
        }

        @Test
        @DisplayName("не участник не может выйти из команды")
        void nonMemberCannotLeave() {
            when(commandRepository.findById(COMMAND_ID))
                    .thenReturn(Optional.of(command(festivalEvent(true, 5))));
            when(commandMemberRepository
                    .existsByCommandMemberId_UserIdAndCommandMemberId_Command_Id(OTHER_USER_ID, COMMAND_ID))
                    .thenReturn(false);

            assertThatThrownBy(() -> commandService.leaveCommand(COMMAND_ID, OTHER_USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("не состоите");
        }

        @Test
        @DisplayName("участник выходит из команды")
        void memberLeaves() {
            when(commandRepository.findById(COMMAND_ID))
                    .thenReturn(Optional.of(command(festivalEvent(true, 5))));
            when(commandMemberRepository
                    .existsByCommandMemberId_UserIdAndCommandMemberId_Command_Id(OTHER_USER_ID, COMMAND_ID))
                    .thenReturn(true);

            commandService.leaveCommand(COMMAND_ID, OTHER_USER_ID);

            verify(commandMemberRepository)
                    .deleteByCommandMemberId_UserIdAndCommandMemberId_Command_Id(OTHER_USER_ID, COMMAND_ID);
        }

        @Test
        @DisplayName("расформировать команду может только создатель")
        void onlyCreatorCanDisband() {
            Command existing = command(festivalEvent(true, 5));
            when(commandRepository.findById(COMMAND_ID)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> commandService.disbandCommand(COMMAND_ID, OTHER_USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Только создатель");

            verify(commandRepository, never()).delete(any());
        }

        @Test
        @DisplayName("создатель расформировывает команду")
        void creatorDisbands() {
            Command existing = command(festivalEvent(true, 5));
            when(commandRepository.findById(COMMAND_ID)).thenReturn(Optional.of(existing));

            commandService.disbandCommand(COMMAND_ID, CREATOR_ID);

            verify(commandRepository).delete(existing);
        }
    }
}
