package ru.rsreu.festivalmanager.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.notification.dto.*;
import ru.rsreu.festivalmanager.notification.entity.*;
import ru.rsreu.festivalmanager.notification.repository.NotificationRepository;
import ru.rsreu.festivalmanager.common.util.CryptoUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final CryptoUtil cryptoUtil;

    @Transactional
    public void processCompanyResult(CompanyResultMsg msg) {
        Notification notification = new Notification();
        NotificationData data = new NotificationData();
        data.setCompanyId(msg.getCompanyId());
        data.setCompanyEmail(msg.getLogin());

        if (msg.getPasswordHash() != null && !msg.getPasswordHash().isBlank()) {
            notification.setNotificationType(NotificationType.APPROVING_COMPANY);
            data.setComment("Доступы отправлены");
            String decryptedPassword = cryptoUtil.decrypt(msg.getPasswordHash());

            String text = "Поздравляем!\n\nВаша компания успешно зарегистрирована в системе.\n" +
                    "Ваши данные для входа:\nЛогин: " + msg.getLogin() + "\nПароль: " + decryptedPassword +
                    "\n\nНикому не сообщайте свой пароль!";
            emailService.sendEmail(msg.getLogin(), "Доступ к аккаунту | FestivalManager", text);
            log.info("Отправлены доступы для компании: {}", msg.getLogin());

        } else {
            notification.setNotificationType(NotificationType.REJECTING_COMPANY);
            data.setComment(msg.getComment());

            String text = "Здравствуйте!\n\nК сожалению, ваша заявка на регистрацию компании была отклонена.\n" +
                    "Причина: " + msg.getComment() + "\n\nПожалуйста, исправьте недочеты и отправьте заявку снова.";
            emailService.sendEmail(msg.getLogin(), "Отказ в регистрации | FestivalManager", text);
            log.info("Отправлен отказ для компании: {}", msg.getLogin());
        }

        notification.setRead(false);
        notification.setCreatedAt(msg.getCreatedAt());

        notification.setData(data);
        notificationRepository.save(notification);
    }

    @Transactional
    public void processFestivalResponse(FestivalResponseMsg msg) {
        NotificationType type = "APPROVED".equals(msg.getStatus()) ?
                NotificationType.APPROVING_FESTIVAL : NotificationType.REJECTING_FESTIVAL;

        Notification notification = new Notification();
        notification.setNotificationType(type);
        notification.setTargetUserId(msg.getTargetUserId());
        notification.setCreatedAt(msg.getUpdatedAt());
        notification.setRead(false);

        NotificationData data = new NotificationData();
        data.setFestivalLocationId(msg.getFestivalLocationId());
        data.setComment(msg.getComment());

        notification.setData(data);
        notificationRepository.save(notification);
    }

    @Transactional
    public void processCommandInvite(CommandInviteMsg msg) {
        Notification notification = new Notification();
        notification.setNotificationType(NotificationType.INVITING_TEAM);
        notification.setTargetUserId(msg.getRelatedUserId());
        notification.setRead(false);
        notification.setCreatedAt(msg.getCreatedAt());

        NotificationData data = new NotificationData();
        data.setEventId(msg.getEventId());
        data.setEventName(msg.getEventName());
        data.setCommandId(msg.getCommandId());
        data.setCommandName(msg.getCommandName());

        notification.setData(data);
        notificationRepository.save(notification);
    }

    @Transactional
    public void processAdminNotification(AdminNotificationMsg msg) {
        Notification notification = new Notification();
        notification.setNotificationType(NotificationType.RESUBMITTING_APPLICATION);
        notification.setTargetUserId(msg.getAgentId());
        notification.setRead(false);
        notification.setCreatedAt(msg.getUpdatedAt());

        NotificationData data = new NotificationData();
        data.setApplicationId(msg.getEntityId());

        notification.setData(data);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, Boolean isRead, Pageable pageable) {
        Page<Notification> notifications;

        if (isRead == null) {
            notifications = notificationRepository.findAllByTargetUserId(userId, pageable);
        } else {
            notifications = notificationRepository.findAllByTargetUserIdAndIsRead(userId, isRead, pageable);
        }

        return notifications.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationDetails(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Уведомление не найдено"));

        if (!notification.getTargetUserId().equals(userId)) {
            throw new IllegalArgumentException("У вас нет прав на просмотр этого уведомления");
        }

        return mapToResponse(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByTargetUserIdAndIsRead(userId, false);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Уведомление не найдено"));

        if (!notification.getTargetUserId().equals(userId)) {
            throw new IllegalArgumentException("У вас нет прав на изменение этого уведомления");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    private NotificationResponse mapToResponse(Notification entity) {
        NotificationData data = entity.getData();

        NotificationDataDto dataDto = null;
        if (data != null) {
            dataDto = NotificationDataDto.builder()
                    .companyId(data.getCompanyId())
                    .companyEmail(data.getCompanyEmail())
                    .festivalLocationId(data.getFestivalLocationId())
                    .comment(data.getComment())
                    .eventId(data.getEventId())
                    .eventName(data.getEventName())
                    .commandId(data.getCommandId())
                    .commandName(data.getCommandName())
                    .relatedUserId(data.getRelatedUserId())
                    .build();
        }

        return NotificationResponse.builder()
                .notificationId(entity.getId())
                .notificationType(entity.getNotificationType().name())
                .targetUserId(entity.getTargetUserId())
                .isRead(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .data(dataDto)
                .build();
    }
}