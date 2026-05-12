package ru.rsreu.festivalmanager.festival.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.festival.dto.TicketPurchaseRequest;
import ru.rsreu.festivalmanager.festival.dto.UserTicketsResponse;
import ru.rsreu.festivalmanager.festival.service.TicketService;

@RestController
@RequestMapping("/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/purchase")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Long> purchaseTicket(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody TicketPurchaseRequest request) {
        return ResponseEntity.ok(ticketService.purchaseTicket(userId, request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'CURATOR')")
    public ResponseEntity<UserTicketsResponse> getMyTickets(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ticketService.getUserTickets(userId));
    }
}
