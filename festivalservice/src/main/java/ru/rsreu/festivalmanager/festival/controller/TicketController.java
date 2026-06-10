package ru.rsreu.festivalmanager.festival.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.festival.dto.festival.FestivalParticipantDto;
import ru.rsreu.festivalmanager.festival.dto.ticket.*;
import ru.rsreu.festivalmanager.festival.entity.ticket.TicketType;
import ru.rsreu.festivalmanager.festival.service.TicketService;

import java.util.List;

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

    @GetMapping("/instances/by-festival/{festivalLocationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TicketTypeInstanceResponse>> getTicketInstancesByFestival(
            @PathVariable Long festivalLocationId) {
        return ResponseEntity.ok(ticketService.getTicketTypeInstancesByFestival(festivalLocationId));
    }

    @GetMapping("/types")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TicketType>> getAllTicketTypes() {
        return ResponseEntity.ok(ticketService.getAllTicketTypes());
    }

    @PostMapping("/types")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<TicketType> createGlobalTicketType(@RequestBody TicketTypeCreateRequest request) {
        return ResponseEntity.ok(ticketService.createTicketType(request.getName()));
    }

    @PostMapping("/instances")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Long> createTicketInstance(
            @RequestBody TicketTypeInstanceCreateRequest request,
            @AuthenticationPrincipal Long companyId) {

        Long instanceId = ticketService.createTicketTypeInstance(request, companyId);
        return ResponseEntity.ok(instanceId);
    }

    @PutMapping("/instances/{instanceId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Void> updateTicketInstance(
            @PathVariable Long instanceId,
            @RequestBody TicketTypeInstanceUpdateRequest request,
            @AuthenticationPrincipal Long companyId) {

        ticketService.updateTicketTypeInstance(instanceId, request.getDescription(), request.getPrice(), companyId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/participants/{festivalLocationId}")
    @PreAuthorize("hasRole('CURATOR')")
    public ResponseEntity<List<FestivalParticipantDto>> getParticipants(
            @PathVariable Long festivalLocationId) {
        return ResponseEntity.ok(ticketService.getParticipantsForFestival(festivalLocationId));
    }

    @GetMapping("/validate-curator-access")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> validateCuratorAccess(
            @RequestParam Long curatorId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(ticketService.validateCuratorAccessToUser(curatorId, userId));
    }

    @PreAuthorize("hasRole('COMPANY')")
    @DeleteMapping("/instances/{instanceId}")
    public ResponseEntity<Void> deleteTicketInstance(
            @PathVariable Long instanceId,
            @AuthenticationPrincipal Long companyId) {

        ticketService.deleteTicketTypeInstance(instanceId, companyId);
        return ResponseEntity.noContent().build();
    }
}
