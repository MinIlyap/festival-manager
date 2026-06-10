package ru.rsreu.festivalmanager.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.event.dto.event.DictionaryCreateRequest;
import ru.rsreu.festivalmanager.event.entity.event.Event;
import ru.rsreu.festivalmanager.event.entity.reward.Reward;
import ru.rsreu.festivalmanager.event.entity.stand.Product;
import ru.rsreu.festivalmanager.event.entity.stand.Stand;
import ru.rsreu.festivalmanager.event.service.DictionaryService;

import java.util.List;

@RestController
@RequestMapping("/events/dictionaries")
@RequiredArgsConstructor
public class DictionaryController {
    private final DictionaryService dictionaryService;

    @GetMapping("/events")
    public ResponseEntity<List<Event>> getEvents() {
        return ResponseEntity.ok(dictionaryService.getAllEvents());
    }

    @PostMapping("/events")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<Long> createEvent(@Valid @RequestBody DictionaryCreateRequest request) {
        return ResponseEntity.ok(dictionaryService.createEventDictionary(request.getName()));
    }

    @GetMapping("/stands")
    public ResponseEntity<List<Stand>> getStands() {
        return ResponseEntity.ok(dictionaryService.getAllStands());
    }

    @PostMapping("/stands")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<Long> createStand(@Valid @RequestBody DictionaryCreateRequest request) {
        return ResponseEntity.ok(dictionaryService.createStandDictionary(request.getName()));
    }

    @GetMapping("/rewards")
    public ResponseEntity<List<Reward>> getRewards() {
        return ResponseEntity.ok(dictionaryService.getAllRewards());
    }

    @PostMapping("/rewards")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<Long> createReward(@Valid @RequestBody DictionaryCreateRequest request) {
        return ResponseEntity.ok(dictionaryService.createRewardDictionary(request.getName()));
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(dictionaryService.getAllProducts());
    }

    @PostMapping("/products")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<Long> createProduct(@Valid @RequestBody DictionaryCreateRequest request) {
        return ResponseEntity.ok(dictionaryService.createProductDictionary(request.getName()));
    }
}