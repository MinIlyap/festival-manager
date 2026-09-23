package ru.rsreu.festivalmanager.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.auth.dto.UserCreationDto;

@FeignClient(name = "user-service", url = "${services.user.url}")
public interface UserServiceClient {
    @PostMapping
    Long createUser(@RequestHeader("Authorization") String token, @RequestBody UserCreationDto request);

    @GetMapping("/check-nickname")
    boolean existsByNickname(@RequestHeader("Authorization") String token, @RequestParam String nickname);

    @GetMapping("/role/{id}")
    String getUserRole(@RequestHeader("Authorization") String token, @PathVariable Long id);
}
