package ru.rsreu.festivalmanager.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.event.dto.FestivalStandCreateRequest;
import ru.rsreu.festivalmanager.event.dto.ProductAddRequest;
import ru.rsreu.festivalmanager.event.service.StandService;

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

    @PostMapping
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Long> createFestivalStand(
            @Valid @RequestBody FestivalStandCreateRequest request,
            @AuthenticationPrincipal Long companyId) {

        return ResponseEntity.ok(standService.createFestivalStand(companyId, request));
    }
}
