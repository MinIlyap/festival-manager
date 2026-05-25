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
import ru.rsreu.festivalmanager.event.dto.FestivalStandCreateRequest;
import ru.rsreu.festivalmanager.event.dto.FestivalStandResponse;
import ru.rsreu.festivalmanager.event.dto.ProductAddRequest;
import ru.rsreu.festivalmanager.event.entity.stand.StandType;
import ru.rsreu.festivalmanager.event.service.StandService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/events/stands")
@RequiredArgsConstructor
public class StandController {

    private final StandService standService;

    /**
     * Открепление стенда от фестиваля (только если фестиваль еще не опубликован)
     */
    @DeleteMapping("/{festivalStandId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<String> detachStandFromFestival(
            @PathVariable Long festivalStandId,
            @AuthenticationPrincipal Long companyId) {

        standService.detachStandFromFestival(festivalStandId, companyId);
        return ResponseEntity.ok("Стенд успешно откреплен от фестиваля");
    }

    /**
     * Добавление конкретного товара на свой стенд
     */
    @PostMapping("/{festivalStandId}/products")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<String> addProductToStand(
            @PathVariable Long festivalStandId,
            @Valid @RequestBody ProductAddRequest request,
            @AuthenticationPrincipal Long companyId) {

        standService.addProductToStand(festivalStandId, companyId, request);
        return ResponseEntity.ok("Товар успешно добавлен на стенд");
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<FestivalStandResponse>> searchStands(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateStart,
            @RequestParam(required = false) StandType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(standService.searchStands(name, cityId, dateStart, type, PageRequest.of(page, size)));
    }

    @GetMapping("/festival/{festivalLocationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FestivalStandResponse>> getStandsByFestival(
            @PathVariable Long festivalLocationId) {
        return ResponseEntity.ok(standService.getStandsByFestival(festivalLocationId));
    }

    @PostMapping
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Long> createFestivalStand(
            @Valid @RequestBody FestivalStandCreateRequest request,
            @AuthenticationPrincipal Long companyId) {

        return ResponseEntity.ok(standService.createFestivalStand(companyId, request));
    }
}
