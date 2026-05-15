package ru.rsreu.festivalmanager.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rsreu.festivalmanager.event.dto.CommandWithMembersResponse;
import ru.rsreu.festivalmanager.event.service.EventInfoService;

import java.util.List;

@RestController
@RequestMapping("/events/{festivalEventId}/info")
@RequiredArgsConstructor
public class EventInfoController {

    private final EventInfoService eventInfoService;

    @GetMapping("/participants")
    @PreAuthorize("hasAnyRole('USER', 'COMPANY', 'ADMIN')")
    public ResponseEntity<List<Long>> getAllParticipants(
            @PathVariable Long festivalEventId) {

        return ResponseEntity.ok(eventInfoService.getAllParticipants(festivalEventId));
    }

    @GetMapping("/commands")
    @PreAuthorize("hasAnyRole('USER', 'COMPANY', 'ADMIN')")
    public ResponseEntity<List<CommandWithMembersResponse>> getCommandsWithMembers(
            @PathVariable Long festivalEventId) {

        return ResponseEntity.ok(eventInfoService.getCommandsWithMembers(festivalEventId));
    }

    @GetMapping("/participants/without-command")
    @PreAuthorize("hasAnyRole('USER', 'COMPANY', 'ADMIN')")
    public ResponseEntity<List<Long>> getParticipantsWithoutCommand(
            @PathVariable Long festivalEventId) {

        return ResponseEntity.ok(eventInfoService.getParticipantsWithoutCommand(festivalEventId));
    }
}
