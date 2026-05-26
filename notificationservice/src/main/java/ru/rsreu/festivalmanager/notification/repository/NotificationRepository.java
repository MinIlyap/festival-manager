package ru.rsreu.festivalmanager.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.notification.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByTargetUserIdAndNotificationTypeIn(Long targetUserId, java.util.List<ru.rsreu.festivalmanager.notification.entity.NotificationType> types, Pageable pageable);

    Page<Notification> findAllByTargetUserIdAndIsReadAndNotificationTypeIn(Long targetUserId, Boolean isRead, java.util.List<ru.rsreu.festivalmanager.notification.entity.NotificationType> types, Pageable pageable);

    long countByTargetUserIdAndIsReadAndNotificationTypeIn(Long targetUserId, boolean isRead, java.util.List<ru.rsreu.festivalmanager.notification.entity.NotificationType> types);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.targetUserId = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);
}