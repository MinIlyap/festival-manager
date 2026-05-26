package ru.rsreu.festivalmanager.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.notification.dto.NotificationResponse;
import ru.rsreu.festivalmanager.notification.service.NotificationService;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Boolean isRead,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String role = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("ROLE_USER");
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, role, isRead, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationResponse> getNotificationDetails(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {

        return ResponseEntity.ok(notificationService.getNotificationDetails(id, userId));
    }

    @PostMapping("/read/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {

        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal Long userId) {
        String role = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("ROLE_USER");
        return ResponseEntity.ok(notificationService.getUnreadCount(userId, role));
    }
}
