package ru.rsreu.festivalmanager.notification.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rsreu.festivalmanager.common.util.CryptoUtil;
import ru.rsreu.festivalmanager.notification.dto.CommandInviteMsg;
import ru.rsreu.festivalmanager.notification.dto.CompanyResultMsg;
import ru.rsreu.festivalmanager.notification.dto.FestivalResponseMsg;
import ru.rsreu.festivalmanager.notification.dto.NotificationResponse;
import ru.rsreu.festivalmanager.notification.entity.Notification;
import ru.rsreu.festivalmanager.notification.entity.NotificationData;
import ru.rsreu.festivalmanager.notification.entity.NotificationType;
import ru.rsreu.festivalmanager.notification.mapper.NotificationMapper;
import ru.rsreu.festivalmanager.notification.mapper.NotificationMapperImpl;
import ru.rsreu.festivalmanager.notification.repository.NotificationRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private EmailService emailService;
    @Mock private CryptoUtil cryptoUtil;

    private final NotificationMapper notificationMapper = new NotificationMapperImpl();

    private NotificationService service() {
        return new NotificationService(notificationRepository, notificationMapper, emailService, cryptoUtil);
    }

    private Notification captureSaved() {
        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(saved.capture());
        return saved.getValue();
    }

    @Nested
    @DisplayName("Результат заявки компании")
    class CompanyResult {

        private CompanyResultMsg msg(String passwordHash, String comment) {
            CompanyResultMsg msg = new CompanyResultMsg();
            msg.setCompanyId(5L);
            msg.setLogin("company@example.com");
            msg.setPasswordHash(passwordHash);
            msg.setComment(comment);
            msg.setCreatedAt(OffsetDateTime.now());
            return msg;
        }

        @Test
        @DisplayName("с паролем — одобрение, пароль расшифровывается и уходит на почту")
        void approvesAndSendsDecryptedPassword() {
            when(cryptoUtil.decrypt("encrypted")).thenReturn("PlainPass1");

            service().processCompanyResult(msg("encrypted", null));

            verify(emailService).sendEmail(eq("company@example.com"), anyString(), contains("PlainPass1"));
            Notification saved = captureSaved();
            assertThat(saved.getNotificationType()).isEqualTo(NotificationType.APPROVING_COMPANY);
            assertThat(saved.isRead()).isFalse();
            assertThat(saved.getData().getCompanyId()).isEqualTo(5L);
            assertThat(saved.getData().getCompanyEmail()).isEqualTo("company@example.com");
        }

        @Test
        @DisplayName("пустой пароль — отказ с причиной, расшифровка не вызывается")
        void rejectsWhenPasswordIsBlank() {
            service().processCompanyResult(msg("   ", "нет документов"));

            verifyNoInteractions(cryptoUtil);
            verify(emailService).sendEmail(eq("company@example.com"), anyString(), contains("нет документов"));
            Notification saved = captureSaved();
            assertThat(saved.getNotificationType()).isEqualTo(NotificationType.REJECTING_COMPANY);
            assertThat(saved.getData().getComment()).isEqualTo("нет документов");
        }

        @Test
        @DisplayName("пароль без значения тоже трактуется как отказ")
        void rejectsWhenPasswordIsNull() {
            service().processCompanyResult(msg(null, "причина"));

            assertThat(captureSaved().getNotificationType()).isEqualTo(NotificationType.REJECTING_COMPANY);
        }
    }

    @Nested
    @DisplayName("Результат заявки на фестиваль")
    class FestivalResponse {

        private FestivalResponseMsg msg(String status) {
            FestivalResponseMsg msg = new FestivalResponseMsg();
            msg.setFestivalLocationId(11L);
            msg.setStatus(status);
            msg.setComment("комментарий");
            msg.setTargetUserId(42L);
            msg.setUpdatedAt(OffsetDateTime.now());
            return msg;
        }

        @Test
        @DisplayName("APPROVED даёт тип одобрения")
        void mapsApprovedStatus() {
            service().processFestivalResponse(msg("APPROVED"));

            Notification saved = captureSaved();
            assertThat(saved.getNotificationType()).isEqualTo(NotificationType.APPROVING_FESTIVAL);
            assertThat(saved.getTargetUserId()).isEqualTo(42L);
            assertThat(saved.getData().getFestivalLocationId()).isEqualTo(11L);
        }

        @Test
        @DisplayName("любой другой статус считается отказом")
        void treatsAnythingElseAsRejection() {
            service().processFestivalResponse(msg("REJECTED"));
            assertThat(captureSaved().getNotificationType()).isEqualTo(NotificationType.REJECTING_FESTIVAL);
        }
    }

    @Nested
    @DisplayName("Приглашение в команду")
    class CommandInvite {

        private CommandInviteMsg msg(OffsetDateTime createdAt) {
            CommandInviteMsg msg = new CommandInviteMsg();
            msg.setEventId(3L);
            msg.setEventName("Турнир");
            msg.setCommandId(9L);
            msg.setCommandName("Альфа");
            msg.setRelatedUserId(42L);
            msg.setCreatedAt(createdAt);
            return msg;
        }

        @Test
        @DisplayName("адресат — приглашаемый, данные события сохраняются")
        void targetsInvitedUser() {
            service().processCommandInvite(msg(OffsetDateTime.now()));

            Notification saved = captureSaved();
            assertThat(saved.getNotificationType()).isEqualTo(NotificationType.INVITING_TEAM);
            assertThat(saved.getTargetUserId()).isEqualTo(42L);
            assertThat(saved.getData().getEventName()).isEqualTo("Турнир");
            assertThat(saved.getData().getCommandName()).isEqualTo("Альфа");
        }

        @Test
        @DisplayName("без даты в сообщении подставляется текущее время")
        void fallsBackToNowWhenDateIsMissing() {
            service().processCommandInvite(msg(null));

            assertThat(captureSaved().getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Чтение и отметка о прочтении")
    class ReadAccess {

        private Notification notification(long id, long targetUserId, boolean read) {
            NotificationData data = new NotificationData();
            data.setCompanyId(5L);
            Notification notification = new Notification();
            notification.setId(id);
            notification.setTargetUserId(targetUserId);
            notification.setRead(read);
            notification.setNotificationType(NotificationType.INVITING_TEAM);
            notification.setCreatedAt(OffsetDateTime.now());
            notification.setData(data);
            return notification;
        }

        @Test
        @DisplayName("чужое уведомление не читается")
        void deniesAccessToSomeoneElsesNotification() {
            when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification(1L, 42L, false)));

            assertThatThrownBy(() -> service().getNotificationDetails(1L, 99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("нет прав");
        }

        @Test
        @DisplayName("чужое уведомление нельзя отметить прочитанным")
        void deniesMarkingSomeoneElsesNotification() {
            when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification(1L, 42L, false)));

            assertThatThrownBy(() -> service().markAsRead(1L, 99L))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("повторная отметка прочитанного не пишет в базу")
        void doesNotSaveAlreadyReadNotification() {
            when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification(1L, 42L, true)));

            service().markAsRead(1L, 42L);

            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("непрочитанное отмечается и сохраняется")
        void marksUnreadNotification() {
            Notification unread = notification(1L, 42L, false);
            when(notificationRepository.findById(1L)).thenReturn(Optional.of(unread));

            service().markAsRead(1L, 42L);

            assertThat(unread.isRead()).isTrue();
            verify(notificationRepository).save(unread);
        }

        @Test
        @DisplayName("тип уведомления и данные доезжают до dto")
        void mapsNotificationToDto() {
            when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification(1L, 42L, false)));

            NotificationResponse response = service().getNotificationDetails(1L, 42L);

            assertThat(response.getNotificationId()).isEqualTo(1L);
            assertThat(response.getNotificationType()).isEqualTo("INVITING_TEAM");
            assertThat(response.getIsRead()).isFalse();
            assertThat(response.getData().getCompanyId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("роль определяет, какие типы уведомлений видно")
        void filtersByRole() {
            service().getUnreadCount(42L, "ROLE_USER");
            service().getUnreadCount(42L, "НЕИЗВЕСТНАЯ_РОЛЬ");

            verify(notificationRepository).countByTargetUserIdAndIsReadAndNotificationTypeIn(
                    42L, false, java.util.List.of(NotificationType.INVITING_TEAM));
            verify(notificationRepository).countByTargetUserIdAndIsReadAndNotificationTypeIn(
                    42L, false, java.util.List.of());
        }
    }
}
