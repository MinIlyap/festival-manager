package ru.rsreu.festivalmanager.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.event.client.FestivalServiceClient;
import ru.rsreu.festivalmanager.event.repository.*;
import ru.rsreu.festivalmanager.event.entity.stand.StandProduct;

@RestController
@RequestMapping("/internal/access/events")
@RequiredArgsConstructor
public class EventAccessController {
    private final FestivalEventRepository festivalEventRepository;
    private final FestivalStandRepository festivalStandRepository;
    private final EventRewardRepository eventRewardRepository;
    private final StandProductRepository standProductRepository;
    private final FestivalServiceClient festivalServiceClient;

    @Value("${internal.system-token}")
    private String systemToken;

    private String getSystemToken() {
        return "Bearer " + systemToken;
    }

    private boolean isFestivalOrganizer(Long festivalLocationId, Long companyId) {
        try {
            return Boolean.TRUE.equals(
                    festivalServiceClient.isCompanyFestivalOrganizer(getSystemToken(), festivalLocationId, companyId)
            );
        } catch (Exception e) {
            return false;
        }
    }

    @PreAuthorize("hasRole('INTERNAL')")
    @GetMapping("/{entityType}/{entityId}")
    public boolean checkEventAccess(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam Long companyId) {

        return switch (entityType) {
            case "FESTIVAL_EVENT" -> festivalEventRepository.findById(entityId)
                    .map(event -> {
                        if (event.getEventInstance().getCompanyId().equals(companyId)) return true;
                        return isFestivalOrganizer(event.getFestivalLocationId(), companyId);
                    })
                    .orElse(false);
            case "FESTIVAL_STAND" -> festivalStandRepository.findById(entityId)
                    .map(stand -> {
                        if (stand.getStandInstance().getCompanyId().equals(companyId)) return true;
                        return isFestivalOrganizer(stand.getFestivalLocationId(), companyId);
                    })
                    .orElse(false);
            case "EVENT_REWARD" -> eventRewardRepository.findById(entityId)
                    .map(reward -> {
                        Long festivalLocationId = reward.getFestivalEvent().getFestivalLocationId();
                        if (reward.getFestivalEvent().getEventInstance().getCompanyId().equals(companyId)) return true;
                        return isFestivalOrganizer(festivalLocationId, companyId);
                    })
                    .orElse(false);
            case "PRODUCT_INSTANCE" -> standProductRepository.findFirstByStandProductId_ProductInstance_Id(entityId)
                    .map(sp -> {
                        Long festivalLocationId = sp.getStandProductId().getFestivalStand().getFestivalLocationId();
                        Long standCompanyId = sp.getStandProductId().getFestivalStand().getStandInstance().getCompanyId();
                        if (standCompanyId.equals(companyId)) return true;
                        return isFestivalOrganizer(festivalLocationId, companyId);
                    })
                    .orElse(false);
            default -> false;
        };
    }
}
