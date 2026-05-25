package ru.rsreu.festivalmanager.festival.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.festival.dto.*;
import ru.rsreu.festivalmanager.festival.entity.festival.AgeLimit;
import ru.rsreu.festivalmanager.festival.service.FestivalService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/festival")
@RequiredArgsConstructor
public class FestivalController {
    private final FestivalService festivalService;

    @GetMapping("/age-limits")
    public ResponseEntity<List<AgeLimit>> getAgeLimits() {
        return ResponseEntity.ok(festivalService.getAgeLimits());
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FestivalResponse>> searchFestivals(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "cityId", required = false) Long cityId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(festivalService.searchFestivals(name, cityId, startDate, endDate));
    }

    @PostMapping("/apply")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Long> applyForFestival(
            @AuthenticationPrincipal Long companyId,
            @Valid @RequestBody FestivalCreateRequest request) {
        return ResponseEntity.ok(festivalService.applyForFestival(request, companyId));
    }

    @GetMapping("/template")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<FestivalCreateRequest> getTemplate(@RequestParam("name") String name) {
        try {
            return ResponseEntity.ok(festivalService.getFestivalTemplate(name));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<FestivalResponse>> getMyCompanyFestivals(@AuthenticationPrincipal Long companyId) {
        return ResponseEntity.ok(festivalService.getFestivalsByCompany(companyId));
    }

    @GetMapping("/curator/my")
    @PreAuthorize("hasRole('CURATOR')")
    public ResponseEntity<List<FestivalResponse>> getMyCuratorFestivals(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(festivalService.getFestivalsByCurator(userId));
    }

    @GetMapping("/{festivalLocationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FestivalResponse> getFestivalById(@PathVariable Long festivalLocationId) {
        return ResponseEntity.ok(festivalService.getFestivalById(festivalLocationId));
    }

    @GetMapping("/company/search")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<CompanyDto> searchCompanyByTin(@RequestParam("tin") String tin) {
        return ResponseEntity.ok(festivalService.searchCompanyByTin(tin));
    }

    @PostMapping("/company")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<CompanyDto> createCompany(@Valid @RequestBody CompanyCreateDto request) {
        return ResponseEntity.ok(festivalService.createCompany(request));
    }
}
