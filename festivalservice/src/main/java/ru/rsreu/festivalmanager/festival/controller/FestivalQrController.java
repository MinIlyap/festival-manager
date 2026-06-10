package ru.rsreu.festivalmanager.festival.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.festival.dto.ticket.QrTicketResponse;
import ru.rsreu.festivalmanager.festival.dto.ticket.VerifyQrTicketRequest;
import ru.rsreu.festivalmanager.festival.dto.ticket.VerifyQrTicketResponse;
import ru.rsreu.festivalmanager.festival.service.FestivalQrService;

@RestController
@RequestMapping("/qr/festivals")
@RequiredArgsConstructor
public class FestivalQrController {

    private final FestivalQrService qrService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/ticket/{ticketId}/generate")
    public ResponseEntity<QrTicketResponse> generateTicket(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(qrService.generateFestivalTicket(ticketId, userId));
    }

    @PreAuthorize("hasRole('CURATOR')")
    @PostMapping("/location/{locationId}/verify")
    public ResponseEntity<VerifyQrTicketResponse> verifyTicket(
            @PathVariable Long locationId,
            @RequestBody VerifyQrTicketRequest request,
            @AuthenticationPrincipal Long curatorId) {
        return ResponseEntity.ok(qrService.verifyFestivalTicket(locationId, request.getToken(), curatorId));
    }
}
