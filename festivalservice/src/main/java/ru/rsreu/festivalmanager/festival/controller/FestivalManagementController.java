package ru.rsreu.festivalmanager.festival.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.festival.dto.*;
import ru.rsreu.festivalmanager.festival.service.FestivalService;

import java.util.List;

@RestController
@RequestMapping("/festival/{locationId}/management")
@RequiredArgsConstructor
public class FestivalManagementController {

    private final FestivalService festivalService;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Void> submitForModeration(
            @PathVariable Long locationId,
            @AuthenticationPrincipal Long companyId) {
        festivalService.submitForModeration(locationId, companyId);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/organizers")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<FestivalOrganizerDto>> getOrganizers(@PathVariable Long locationId) {
        return ResponseEntity.ok(festivalService.getOrganizers(locationId));
    }

    @PostMapping("/organizers")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<FestivalOrganizerDto> addOrganizer(
            @PathVariable Long locationId,
            @Valid @RequestBody AddOrganizerRequest request,
            @AuthenticationPrincipal Long companyId) {
        return ResponseEntity.ok(festivalService.addOrganizer(locationId, request.getTin(), companyId));
    }

    @DeleteMapping("/organizers/{companyId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Void> removeOrganizer(
            @PathVariable Long locationId,
            @PathVariable Long companyId,
            @AuthenticationPrincipal Long requestingCompanyId) {
        festivalService.removeOrganizer(locationId, companyId, requestingCompanyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/curators")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<FestivalCuratorDto>> getCurators(@PathVariable Long locationId) {
        return ResponseEntity.ok(festivalService.getCurators(locationId));
    }

    @PostMapping("/curators")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<FestivalCuratorDto> addCurator(
            @PathVariable Long locationId,
            @Valid @RequestBody AddCuratorRequest request,
            @AuthenticationPrincipal Long companyId) {
        return ResponseEntity.ok(festivalService.addCurator(locationId, request.getEmail(), companyId));
    }

    @DeleteMapping("/curators/{curatorId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Void> removeCurator(
            @PathVariable Long locationId,
            @PathVariable Long curatorId,
            @AuthenticationPrincipal Long companyId) {
        festivalService.removeCurator(locationId, curatorId, companyId);
        return ResponseEntity.noContent().build();
    }
}
