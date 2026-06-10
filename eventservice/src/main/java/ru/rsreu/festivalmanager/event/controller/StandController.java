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
import ru.rsreu.festivalmanager.event.dto.stand.FestivalStandCreateRequest;
import ru.rsreu.festivalmanager.event.dto.stand.FestivalStandResponse;
import ru.rsreu.festivalmanager.event.dto.stand.FestivalStandUpdateRequest;
import ru.rsreu.festivalmanager.event.dto.stand.ProductAddRequest;
import ru.rsreu.festivalmanager.event.dto.stand.StandProductResponse;
import ru.rsreu.festivalmanager.event.entity.stand.StandType;
import ru.rsreu.festivalmanager.event.service.StandService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/events/stands")
@RequiredArgsConstructor
public class StandController {
    private final StandService standService;

    @PutMapping("/{festivalStandId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Void> updateFestivalStand(
            @PathVariable Long festivalStandId,
            @Valid @RequestBody FestivalStandUpdateRequest request,
            @AuthenticationPrincipal Long companyId) {

        standService.updateFestivalStand(festivalStandId, companyId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{festivalStandId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<String> detachStandFromFestival(
            @PathVariable Long festivalStandId,
            @AuthenticationPrincipal Long companyId) {

        standService.detachStandFromFestival(festivalStandId, companyId);
        return ResponseEntity.ok("Стенд успешно откреплен от фестиваля");
    }

    @PostMapping("/{festivalStandId}/products")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<String> addProductToStand(
            @PathVariable Long festivalStandId,
            @Valid @RequestBody ProductAddRequest request,
            @AuthenticationPrincipal Long companyId) {

        standService.addProductToStand(festivalStandId, companyId, request);
        return ResponseEntity.ok("Товар успешно добавлен на стенд");
    }

    @GetMapping("/{festivalStandId}/products")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StandProductResponse>> getProductsByStand(
            @PathVariable Long festivalStandId) {

        return ResponseEntity.ok(standService.getProductsByStand(festivalStandId));
    }

    @DeleteMapping("/{festivalStandId}/products/{productInstanceId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<String> removeProductFromStand(
            @PathVariable Long festivalStandId,
            @PathVariable Long productInstanceId,
            @AuthenticationPrincipal Long companyId) {

        standService.removeProductFromStand(festivalStandId, productInstanceId, companyId);
        return ResponseEntity.ok("Товар удалён со стенда");
    }

    @PutMapping("/{festivalStandId}/products/{productInstanceId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<String> updateProductOnStand(
            @PathVariable Long festivalStandId,
            @PathVariable Long productInstanceId,
            @Valid @RequestBody ProductAddRequest request,
            @AuthenticationPrincipal Long companyId) {

        standService.updateProductOnStand(festivalStandId, productInstanceId, companyId, request);
        return ResponseEntity.ok("Товар обновлён");
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
