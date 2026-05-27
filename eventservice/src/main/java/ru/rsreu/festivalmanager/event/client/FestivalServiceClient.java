package ru.rsreu.festivalmanager.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import ru.rsreu.festivalmanager.event.dto.CompanyBriefDto;

import java.util.List;

@FeignClient(name = "festival-service", url = "http://localhost:8085")
public interface FestivalServiceClient {

    @GetMapping("/internal/festivals/visible-ids")
    List<Long> getAllVisibleFestivalLocationIds(
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/internal/festivals/city/{cityId}")
    List<Long> getFestivalLocationIdsByCity(
            @RequestHeader("Authorization") String token,
            @PathVariable Long cityId
    );

    @GetMapping("/internal/festivals/{festivalLocationId}/visibility")
    Boolean isFestivalVisible(
            @RequestHeader("Authorization") String token,
            @PathVariable Long festivalLocationId
    );

    @GetMapping("/internal/festivals/{festivalLocationId}/tickets/check")
    Boolean checkUserHasValidTicket(
            @RequestHeader("Authorization") String token,
            @PathVariable Long festivalLocationId,
            @RequestParam("userId") Long userId
    );

    @GetMapping("/internal/festivals/{festivalLocationId}/companies/check")
    Boolean isCompanyFestivalOrganizer(
            @RequestHeader("Authorization") String token,
            @PathVariable Long festivalLocationId,
            @RequestParam("companyId") Long companyId
    );

    @GetMapping("/internal/festivals/company/{companyId}")
    CompanyBriefDto getCompanyById(
            @RequestHeader("Authorization") String token,
            @PathVariable Long companyId
    );
}
