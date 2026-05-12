package ru.rsreu.festivalmanager.application.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.application.dto.ReviewRequestDto;
import ru.rsreu.festivalmanager.application.entity.Application;
import ru.rsreu.festivalmanager.application.entity.ApplicationStatus;
import ru.rsreu.festivalmanager.application.entity.ApplicationType;
import ru.rsreu.festivalmanager.application.service.ApplicationService;

import java.util.List;

@RestController
@RequestMapping("/application/admin")
@RequiredArgsConstructor
public class ApplicationAdminController {

    private final ApplicationService applicationService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Application>> getAllApplications(
            @RequestParam(required = false) ApplicationType type,
            @RequestParam(required = false) ApplicationStatus status) {

        return ResponseEntity.ok(applicationService.getAllApplicationsForAdmin(type, status));
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Application>> getMyHistory(
            @AuthenticationPrincipal Long adminId,
            @RequestParam(required = false) ApplicationType type,
            @RequestParam(required = false) ApplicationStatus status) {

        return ResponseEntity.ok(applicationService.getAdminPersonalHistory(adminId, type, status));
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reviewApplication(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReviewRequestDto dto,
            @AuthenticationPrincipal Long adminId) {

        applicationService.reviewApplication(id, dto, adminId);
        return ResponseEntity.ok().build();
    }
}