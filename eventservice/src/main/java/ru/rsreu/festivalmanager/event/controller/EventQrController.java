package ru.rsreu.festivalmanager.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.event.dto.QrRegistrationResponse;
import ru.rsreu.festivalmanager.event.dto.VerifyQrRegistrationRequest;
import ru.rsreu.festivalmanager.event.dto.VerifyQrRegistrationResponse;
import ru.rsreu.festivalmanager.event.service.EventQrService;

@RestController
@RequestMapping("/qr/events")
@RequiredArgsConstructor
public class EventQrController {

    private final EventQrService qrService;

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/festival-event/{festivalEventId}/generate")
    public ResponseEntity<QrRegistrationResponse> generateTicket(
            @PathVariable Long festivalEventId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(qrService.generateEventTicket(festivalEventId, userId));
    }

    @PreAuthorize("hasAuthority('COMPANY')")
    @PostMapping("/festival-event/{festivalEventId}/verify")
    public ResponseEntity<VerifyQrRegistrationResponse> verifyTicket(
            @PathVariable Long festivalEventId,
            @RequestBody VerifyQrRegistrationRequest request,
            @AuthenticationPrincipal Long companyId) {
        return ResponseEntity.ok(qrService.verifyEventTicket(festivalEventId, request.getToken(), companyId));
    }
}
