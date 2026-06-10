package ru.rsreu.festivalmanager.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.event.dto.reward.EventRewardResponse;
import ru.rsreu.festivalmanager.event.dto.reward.RewardAddRequest;
import ru.rsreu.festivalmanager.event.dto.reward.RewardDistributionRequest;
import ru.rsreu.festivalmanager.event.service.RewardService;

import java.util.List;

@RestController
@RequestMapping("/events/rewards")
@RequiredArgsConstructor
public class RewardController {
    private final RewardService rewardService;

    @GetMapping("/by-event/{festivalEventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventRewardResponse>> getRewardsByEvent(
            @PathVariable Long festivalEventId) {

        return ResponseEntity.ok(rewardService.getRewardsByEvent(festivalEventId));
    }

    @DeleteMapping("/{eventRewardId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<String> removeRewardFromEvent(
            @PathVariable Long eventRewardId,
            @AuthenticationPrincipal Long companyId) {

        rewardService.removeRewardFromEvent(eventRewardId, companyId);
        return ResponseEntity.ok("Награда удалена");
    }

    @PostMapping("/{festivalEventId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Long> addRewardToEvent(
            @PathVariable Long festivalEventId,
            @Valid @RequestBody RewardAddRequest request,
            @AuthenticationPrincipal Long companyId) {

        return ResponseEntity.ok(rewardService.addRewardToEvent(festivalEventId, companyId, request));
    }


    @PostMapping("/{eventRewardId}/distribute/users")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<String> distributeRewardToUsers(
            @PathVariable Long eventRewardId,
            @Valid @RequestBody RewardDistributionRequest request,
            @AuthenticationPrincipal Long companyId) {

        rewardService.distributeRewardToUsers(eventRewardId, companyId, request.getUserIds());
        return ResponseEntity.ok("Награды успешно распределены участникам");
    }


    @PostMapping("/{eventRewardId}/distribute/commands/{commandId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<String> distributeRewardToCommand(
            @PathVariable Long eventRewardId,
            @PathVariable Long commandId,
            @AuthenticationPrincipal Long companyId) {

        rewardService.distributeRewardToCommand(eventRewardId, companyId, commandId);
        return ResponseEntity.ok("Награды успешно распределены всей команде");
    }

    @DeleteMapping("/{eventRewardId}/revoke/users/{userId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<String> revokeRewardFromUser(
            @PathVariable Long eventRewardId,
            @PathVariable Long userId,
            @AuthenticationPrincipal Long companyId) {

        rewardService.revokeRewardFromUser(eventRewardId, companyId, userId);
        return ResponseEntity.ok("Награда отозвана у пользователя");
    }

    @DeleteMapping("/{eventRewardId}/revoke/commands/{commandId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<String> revokeRewardFromCommand(
            @PathVariable Long eventRewardId,
            @PathVariable Long commandId,
            @AuthenticationPrincipal Long companyId) {

        rewardService.revokeRewardFromCommand(eventRewardId, companyId, commandId);
        return ResponseEntity.ok("Награда отозвана у всей команды");
    }
}