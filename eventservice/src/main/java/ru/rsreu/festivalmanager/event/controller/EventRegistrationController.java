package ru.rsreu.festivalmanager.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.event.service.EventRegistrationService;

@RestController
@RequestMapping("/events/registrations")
@RequiredArgsConstructor
public class EventRegistrationController {
    private final EventRegistrationService registrationService;

    @PostMapping("/{festivalEventId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> registerToEvent(
            @PathVariable Long festivalEventId,
            @AuthenticationPrincipal Long userId) {

        registrationService.registerUserToEvent(festivalEventId, userId);
        return ResponseEntity.ok("Вы успешно зарегистрировались на мероприятие!");
    }
}
