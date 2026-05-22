package ru.rsreu.festivalmanager.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.event.repository.*;

@RestController
@RequestMapping("/internal/access/events")
@RequiredArgsConstructor
public class EventAccessController {

    private final FestivalEventRepository festivalEventRepository;
    private final FestivalStandRepository festivalStandRepository;
    private final EventRewardRepository eventRewardRepository;

    @PreAuthorize("hasRole('INTERNAL')")
    @GetMapping("/{entityType}/{entityId}")
    public boolean checkEventAccess(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam Long companyId) {

        return switch (entityType) {
            case "FESTIVAL_EVENT" -> festivalEventRepository.findById(entityId)
                    .map(event -> event.getEventInstance().getCompanyId().equals(companyId))
                    .orElse(false);
            case "FESTIVAL_STAND" -> festivalStandRepository.findById(entityId)
                    .map(stand -> stand.getStandInstance().getCompanyId().equals(companyId))
                    .orElse(false);
            case "EVENT_REWARD" -> eventRewardRepository.findById(entityId)
                    .map(reward -> reward.getFestivalEvent().getEventInstance().getCompanyId().equals(companyId))
                    .orElse(false);
            default -> false;
        };
    }
}
