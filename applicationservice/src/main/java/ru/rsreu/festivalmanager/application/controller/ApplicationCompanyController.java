package ru.rsreu.festivalmanager.application.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.application.entity.Application;
import ru.rsreu.festivalmanager.application.entity.ApplicationStatus;
import ru.rsreu.festivalmanager.application.service.ApplicationService;

@RestController
@RequestMapping("/application/my")
@RequiredArgsConstructor
public class ApplicationCompanyController {
    private final ApplicationService applicationService;

    @GetMapping
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Page<Application>> getMyApplications(
            @AuthenticationPrincipal Long companyId,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(applicationService.getMyApplications(companyId, status,
                PageRequest.of(page, size)));
    }
}