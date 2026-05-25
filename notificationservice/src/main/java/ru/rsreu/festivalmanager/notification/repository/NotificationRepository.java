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
    Page<Notification> findAllByTargetUserId(Long targetUserId, Pageable pageable);

    Page<Notification> findAllByTargetUserIdAndIsRead(Long targetUserId, Boolean isRead, Pageable pageable);

    long countByTargetUserIdAndIsRead(Long targetUserId, boolean isRead);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.targetUserId = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);
}