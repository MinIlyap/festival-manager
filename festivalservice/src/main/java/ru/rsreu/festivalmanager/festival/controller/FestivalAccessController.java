package ru.rsreu.festivalmanager.festival.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.festival.service.FestivalService;

@RestController
@RequestMapping("/internal/access/festivals")
@RequiredArgsConstructor
public class FestivalAccessController {

    private final FestivalService festivalService;

    @PreAuthorize("hasRole('INTERNAL')")
    @GetMapping("/{entityType}/{entityId}")
    public boolean checkFestivalAccess(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam Long companyId) {

        return festivalService.hasAccessForFestival(entityType, entityId, companyId);
    }
}
