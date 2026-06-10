package ru.rsreu.festivalmanager.festival.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.festival.dto.festival.BlacklistStatusDto;
import ru.rsreu.festivalmanager.festival.dto.festival.UserProfileDto;

@FeignClient(name = "user-service", url = "${services.user.url}")
public interface UserServiceClient {
    @GetMapping("/blacklist/check/{userId}")
    boolean checkBlackListStatus(@RequestHeader("Authorization") String token, @PathVariable Long userId);

    @GetMapping("/blacklist/status/{userId}")
    BlacklistStatusDto getBlacklistStatus(@RequestHeader("Authorization") String token, @PathVariable Long userId);

    @GetMapping("/profile/{id}")
    UserProfileDto getUserProfile(@RequestHeader("Authorization") String token, @PathVariable Long id);
}