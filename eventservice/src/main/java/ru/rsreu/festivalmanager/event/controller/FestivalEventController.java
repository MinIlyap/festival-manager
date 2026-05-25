package ru.rsreu.festivalmanager.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.event.dto.FestivalEventCreateRequest;
import ru.rsreu.festivalmanager.event.dto.FestivalEventResponse;
import ru.rsreu.festivalmanager.event.entity.event.EventType;
import ru.rsreu.festivalmanager.event.service.FestivalEventService;
import ru.rsreu.festivalmanager.event.service.UserEventProfileService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class FestivalEventController {

    private final FestivalEventService festivalEventService;
    private final UserEventProfileService userEventProfileService;

    @PostMapping
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Long> createFestivalEvent(
            @Valid @RequestBody FestivalEventCreateRequest request,
            @AuthenticationPrincipal Long companyId) {

        return ResponseEntity.ok(festivalEventService.createFestivalEvent(companyId, request));
    }

    @DeleteMapping("/{festivalEventId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<String> detachEvent(
            @PathVariable Long festivalEventId,
            @AuthenticationPrincipal Long companyId) {

        festivalEventService.detachEventFromFestival(festivalEventId, companyId);
        return ResponseEntity.ok("Мероприятие откреплено");
    }

    @GetMapping("/my/created")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<FestivalEventResponse>> getMyCompanyEvents(
            @AuthenticationPrincipal Long companyId) {
        return ResponseEntity.ok(festivalEventService.getEventsByCompany(companyId));
    }

    @GetMapping("/festival/{festivalLocationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FestivalEventResponse>> getEventsByFestival(
            @PathVariable Long festivalLocationId,
            @RequestParam(value = "type", required = false) EventType type) {

        return ResponseEntity.ok(userEventProfileService.getEventsForFestival(festivalLocationId, type));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<FestivalEventResponse>> searchFestivalEvents(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "cityId", required = false) Long cityId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) EventType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(festivalEventService.searchFestivalEvents(
                name, cityId, startDate, endDate, type, PageRequest.of(page, size)
        ));
    }
}