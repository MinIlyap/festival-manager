package ru.rsreu.festivalmanager.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "festival-service", url = "http://localhost:8085/company")
public interface FestivalServiceClient {

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
}
