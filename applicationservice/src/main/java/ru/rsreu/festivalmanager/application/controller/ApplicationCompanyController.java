package ru.rsreu.festivalmanager.application.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.application.entity.Application;
import ru.rsreu.festivalmanager.application.entity.ApplicationStatus;
import ru.rsreu.festivalmanager.application.service.ApplicationService;

import java.util.List;

@RestController
@RequestMapping("/application/my")
@RequiredArgsConstructor
public class ApplicationCompanyController {
    private final ApplicationService applicationService;

    @GetMapping
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<Application>> getMyApplications(
            @AuthenticationPrincipal Long companyId,
            @RequestParam(required = false) ApplicationStatus status) {

        return ResponseEntity.ok(applicationService.getMyApplications(companyId, status));
    }
}