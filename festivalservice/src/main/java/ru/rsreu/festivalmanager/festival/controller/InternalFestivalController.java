package ru.rsreu.festivalmanager.festival.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.festival.dto.CompanyDto;
import ru.rsreu.festivalmanager.festival.service.FestivalService;
import ru.rsreu.festivalmanager.festival.service.TicketService;

import java.util.List;

@RestController
@RequestMapping("/internal/festivals")
@RequiredArgsConstructor
public class InternalFestivalController {

    private final FestivalService festivalService;
    private final TicketService ticketService;

    @GetMapping("/city/{cityId}")
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<List<Long>> getFestivalLocationIdsByCity(@PathVariable Long cityId) {
        return ResponseEntity.ok(festivalService.getFestivalLocationIdsByCity(cityId));
    }

    @GetMapping("/{festivalLocationId}/visibility")
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<Boolean> isFestivalVisible(@PathVariable Long festivalLocationId) {
        return ResponseEntity.ok(festivalService.isFestivalVisible(festivalLocationId));
    }

    @GetMapping("/{festivalLocationId}/tickets/check")
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<Boolean> checkUserHasValidTicket(
            @PathVariable Long festivalLocationId,
            @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(ticketService.hasValidTicket(festivalLocationId, userId));
    }

    @GetMapping("/{festivalLocationId}/companies/check")
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<Boolean> isCompanyFestivalOrganizer(
            @PathVariable Long festivalLocationId,
            @RequestParam("companyId") Long companyId) {
        return ResponseEntity.ok(festivalService.isCompanyFestivalOrganizer(festivalLocationId, companyId));
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<CompanyDto> getCompanyById(@PathVariable Long companyId) {
        return ResponseEntity.ok(festivalService.getCompanyById(companyId));
    }
}
