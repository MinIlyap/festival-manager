package ru.rsreu.festivalmanager.festival.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.festival.dto.PlatformInfoDto;

import java.util.List;

@FeignClient(name = "location-service", url = "${services.location.url}")
public interface LocationServiceClient {

    @GetMapping("/platform-instance/city/{cityId}")
    List<Long> getPlatformIdsByCity(@RequestHeader("Authorization") String token, @PathVariable Long cityId);

    @GetMapping("/platform-instance/check/{id}")
    boolean checkPlatformExists(@RequestHeader("Authorization") String token, @PathVariable Long id);

    @GetMapping("/platform-instance/{id}")
    PlatformInfoDto getPlatformInstanceById(@RequestHeader("Authorization") String token, @PathVariable Long id);
}
