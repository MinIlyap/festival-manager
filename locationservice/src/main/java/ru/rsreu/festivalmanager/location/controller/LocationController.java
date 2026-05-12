package ru.rsreu.festivalmanager.location.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.location.dto.*;
import ru.rsreu.festivalmanager.location.service.LocationService;

import java.util.List;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @GetMapping("/city/check/{id}")
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<Boolean> checkCityExists(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.checkCityExists(id));
    }

    @GetMapping("/city/search")
    @PreAuthorize("hasAnyRole('USER', 'COMPANY', 'ADMIN')")
    public ResponseEntity<List<CityResponse>> searchCities(@RequestParam("name") String name) {
        return ResponseEntity.ok(locationService.searchCities(name));
    }

    @PostMapping("/city")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CityResponse> addCity(@Valid @RequestBody CityCreateRequest request) {
        return ResponseEntity.ok(locationService.addCity(request));
    }

    @GetMapping("/platform-instance/check/{id}")
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<Boolean> checkPlatformExists(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.isPlatformInstanceExistsById(id));
    }

    @GetMapping("/platform-instance/city/{cityId}")
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<List<Long>> getPlatformIdsByCity(@PathVariable Long cityId) {
        return ResponseEntity.ok(locationService.getPlatformIdsByCity(cityId));
    }

    @GetMapping("/platform-instance/search")
    @PreAuthorize("hasAnyRole('USER', 'COMPANY', 'ADMIN')")
    public ResponseEntity<List<PlatformInstanceResponse>> searchPlatformInstances(
            @RequestParam("cityId") Long cityId,
            @RequestParam("platformName") String platformName) {
        return ResponseEntity.ok(locationService.searchPlatformInstances(cityId, platformName));
    }

    @PostMapping("/platform-instance")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<PlatformInstanceResponse> createPlatformInstance(
            @Valid @RequestBody PlatformInstanceCreateRequest request) {
        return ResponseEntity.ok(locationService.createOrGetPlatformInstance(request));
    }

    @PutMapping("/platform-instance/{id}")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<PlatformInstanceResponse> updatePlatformInstance(
            @PathVariable Long id,
            @Valid @RequestBody PlatformInstanceUpdateRequest request) {
        return ResponseEntity.ok(locationService.updatePlatformInstance(id, request));
    }
}
