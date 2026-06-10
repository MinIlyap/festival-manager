package ru.rsreu.festivalmanager.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rsreu.festivalmanager.event.dto.event.UserFestivalEventsResponse;
import ru.rsreu.festivalmanager.event.dto.reward.UserFestivalRewardsResponse;
import ru.rsreu.festivalmanager.event.service.UserEventProfileService;

import java.util.List;

@RestController
@RequestMapping("/events/my")
@RequiredArgsConstructor
public class UserEventProfileController {
    private final UserEventProfileService userEventProfileService;

    @GetMapping("/attended")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UserFestivalEventsResponse>> getMyAttendedEvents(
            @AuthenticationPrincipal Long userId) {

        return ResponseEntity.ok(userEventProfileService.getMyAttendedEvents(userId));
    }

    @GetMapping("/rewards")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UserFestivalRewardsResponse>> getMyRewards(
            @AuthenticationPrincipal Long userId) {

        return ResponseEntity.ok(userEventProfileService.getMyRewards(userId));
    }
}