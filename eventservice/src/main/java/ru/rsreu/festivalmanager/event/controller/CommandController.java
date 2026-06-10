package ru.rsreu.festivalmanager.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.event.service.CommandService;

@RestController
@RequestMapping("/events/commands")
@RequiredArgsConstructor
public class CommandController {
    private final CommandService commandService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Long> createCommand(
            @RequestParam("festivalEventId") Long festivalEventId,
            @RequestParam("name") String name,
            @AuthenticationPrincipal Long userId) {

        return ResponseEntity.ok(commandService.createCommand(festivalEventId, name, userId));
    }

    @PostMapping("/{commandId}/invite")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> inviteUser(
            @PathVariable Long commandId,
            @RequestParam("targetUserId") Long targetUserId,
            @AuthenticationPrincipal Long senderId) {

        commandService.inviteUserToCommand(commandId, targetUserId, senderId);
        return ResponseEntity.ok("Приглашение отправлено");
    }

    @PostMapping("/{commandId}/join")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> joinCommand(
            @PathVariable Long commandId,
            @AuthenticationPrincipal Long userId) {

        commandService.joinCommand(commandId, userId);
        return ResponseEntity.ok("Вы успешно присоединились к команде");
    }

    @PostMapping("/{commandId}/leave")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> leaveCommand(
            @PathVariable Long commandId,
            @AuthenticationPrincipal Long userId) {

        commandService.leaveCommand(commandId, userId);
        return ResponseEntity.ok("Вы покинули команду");
    }

    @DeleteMapping("/{commandId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> disbandCommand(
            @PathVariable Long commandId,
            @AuthenticationPrincipal Long userId) {

        commandService.disbandCommand(commandId, userId);
        return ResponseEntity.ok("Команда расформирована");
    }
}
