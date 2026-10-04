package ru.rsreu.festivalmanager.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.notification.dto.*;
import ru.rsreu.festivalmanager.notification.entity.*;
import ru.rsreu.festivalmanager.notification.mapper.NotificationMapper;
import ru.rsreu.festivalmanager.notification.repository.NotificationRepository;
import ru.rsreu.festivalmanager.common.util.CryptoUtil;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;
    private final CryptoUtil cryptoUtil;

    private static final Map<String, List<NotificationType>> ROLE_TYPES = Map.of(
        "ROLE_COMPANY",  List.of(NotificationType.APPROVING_COMPANY, NotificationType.REJECTING_COMPANY,
                                  NotificationType.APPROVING_FESTIVAL, NotificationType.REJECTING_FESTIVAL),
        "ROLE_ADMIN",    List.of(NotificationType.RESUBMITTING_APPLICATION),
        "ROLE_USER",     List.of(NotificationType.INVITING_TEAM),
        "ROLE_CURATOR",  List.of(NotificationType.INVITING_TEAM)
    );

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

        Notification notification = Notification.builder()
                .notificationType(type)
                .targetUserId(msg.getTargetUserId())
                .createdAt(msg.getUpdatedAt())
                .isRead(false)
                .data(NotificationData.builder()
                        .festivalLocationId(msg.getFestivalLocationId())
                        .comment(msg.getComment())
                        .build())
                .build();

        notificationRepository.save(notification);
    }

    @Transactional
    public void processCommandInvite(CommandInviteMsg msg) {
        Notification notification = Notification.builder()
                .notificationType(NotificationType.INVITING_TEAM)
                .targetUserId(msg.getRelatedUserId())
                .isRead(false)
                .createdAt(msg.getCreatedAt() != null ? msg.getCreatedAt() : OffsetDateTime.now())
                .data(NotificationData.builder()
                        .eventId(msg.getEventId())
                        .eventName(msg.getEventName())
                        .commandId(msg.getCommandId())
                        .commandName(msg.getCommandName())
                        .build())
                .build();

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
        data.setApplicationId(msg.getApplicationId());
        if ("FESTIVAL_APPLICATION".equals(msg.getType())) {
            data.setFestivalLocationId(msg.getEntityId());
        }

        notification.setData(data);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, String role, Boolean isRead, Pageable pageable) {
        List<NotificationType> types = ROLE_TYPES.getOrDefault(role, List.of());
        Page<Notification> notifications;

        if (isRead == null) {
            notifications = notificationRepository.findAllByTargetUserIdAndNotificationTypeIn(userId, types, pageable);
        } else {
            notifications = notificationRepository.findAllByTargetUserIdAndIsReadAndNotificationTypeIn(userId, isRead, types, pageable);
        }

        return notifications.map(notificationMapper::toNotificationResponse);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationDetails(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Уведомление не найдено"));

        if (!notification.getTargetUserId().equals(userId)) {
            throw new IllegalArgumentException("У вас нет прав на просмотр этого уведомления");
        }

        return notificationMapper.toNotificationResponse(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId, String role) {
        List<NotificationType> types = ROLE_TYPES.getOrDefault(role, List.of());
        return notificationRepository.countByTargetUserIdAndIsReadAndNotificationTypeIn(userId, false, types);
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

}
