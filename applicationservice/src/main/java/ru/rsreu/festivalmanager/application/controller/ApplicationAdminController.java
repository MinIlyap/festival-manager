package ru.rsreu.festivalmanager.application.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.application.dto.ReviewRequestDto;
import ru.rsreu.festivalmanager.application.entity.Application;
import ru.rsreu.festivalmanager.application.entity.ApplicationStatus;
import ru.rsreu.festivalmanager.application.entity.ApplicationType;
import ru.rsreu.festivalmanager.application.service.ApplicationService;

@RestController
@RequestMapping("/application/admin")
@RequiredArgsConstructor
public class ApplicationAdminController {
    private final ApplicationService applicationService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Application> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Application>> getAllApplications(
            @RequestParam(required = false) ApplicationType type,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(applicationService.getAllApplicationsForAdmin(type, status,
                PageRequest.of(page, size)));
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Application>> getMyHistory(
            @AuthenticationPrincipal Long adminId,
            @RequestParam(required = false) ApplicationType type,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(applicationService.getAdminPersonalHistory(adminId, type, status,
                PageRequest.of(page, size)));
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reviewApplication(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequestDto dto,
            @AuthenticationPrincipal Long adminId) {

        applicationService.reviewApplication(id, dto, adminId);
        return ResponseEntity.ok().build();
    }
}
