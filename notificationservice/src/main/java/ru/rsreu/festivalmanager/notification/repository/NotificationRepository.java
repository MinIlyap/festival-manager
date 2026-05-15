package ru.rsreu.festivalmanager.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.notification.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByTargetUserId(Long targetUserId, Pageable pageable);

    Page<Notification> findAllByTargetUserIdAndIsRead(Long targetUserId, Boolean isRead, Pageable pageable);
}