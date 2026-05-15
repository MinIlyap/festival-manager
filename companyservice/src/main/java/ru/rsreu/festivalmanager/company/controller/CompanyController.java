package ru.rsreu.festivalmanager.company.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.company.dto.CompanyCreateRequest;
import ru.rsreu.festivalmanager.company.dto.CompanyResponse;
import ru.rsreu.festivalmanager.company.dto.CompanyUpdateRequest;
import ru.rsreu.festivalmanager.company.service.CompanyService;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @PostMapping
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<Long> createInactiveCompany(@Valid @RequestBody CompanyCreateRequest request) {
        return ResponseEntity.ok(companyService.createInactiveCompany(request));
    }

    @PutMapping("/activate/{id}")
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<Void> activateCompany(@PathVariable Long id) {
        companyService.activateCompany(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/email/{id}")
    @PreAuthorize("hasRole('INTERNAL')")
    public void updateCompanyEmail(
            @PathVariable Long id,
            @RequestParam("newEmail") String newEmail) {

        companyService.updateEmail(id, newEmail);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INTERNAL', 'ADMIN')")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('INTERNAL', 'ADMIN')")
    public ResponseEntity<CompanyResponse> getCompanyByTin(@RequestParam("tin") String tin) {
        return ResponseEntity.ok(companyService.getCompanyByTin(tin));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<CompanyResponse> getMe(@AuthenticationPrincipal Long companyId) {
        return ResponseEntity.ok(companyService.getCompanyById(companyId));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<CompanyResponse> updateMe(
            @AuthenticationPrincipal Long companyId,
            @Valid @RequestBody CompanyUpdateRequest request) {
        return ResponseEntity.ok(companyService.updateCompany(companyId, request));
    }
}
