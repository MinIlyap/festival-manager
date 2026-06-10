package ru.rsreu.festivalmanager.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "festival-service", url = "${services.festival.url}")
public interface FestivalServiceClient {
    @GetMapping("/ticket/validate-curator-access")
    boolean validateCuratorAccessToUser(
            @RequestHeader("Authorization") String token,
            @RequestParam("curatorId") Long curatorId,
            @RequestParam("userId") Long userId
    );
}
