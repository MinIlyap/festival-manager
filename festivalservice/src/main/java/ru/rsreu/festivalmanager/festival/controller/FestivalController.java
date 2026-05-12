package ru.rsreu.festivalmanager.festival.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.festival.dto.FestivalCreateRequest;
import ru.rsreu.festivalmanager.festival.dto.FestivalResponse;
import ru.rsreu.festivalmanager.festival.service.FestivalService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/festival")
@RequiredArgsConstructor
public class FestivalController {
    private final FestivalService festivalService;

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
}
