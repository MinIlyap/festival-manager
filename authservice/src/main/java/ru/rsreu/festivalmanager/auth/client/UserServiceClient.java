package ru.rsreu.festivalmanager.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.auth.dto.UserCreationDto;

@FeignClient(name = "user-service", url = "http://localhost:8089/user")
@Component
public interface UserServiceClient {

    @PostMapping
    Long createUser(@RequestHeader("Authorization") String token, @RequestBody UserCreationDto request);

    @GetMapping("/check-nickname")
    boolean existsByNickname(@RequestHeader("Authorization") String token, @RequestParam String nickname);

    @GetMapping("/role/{id}")
    String getUserRole(@RequestHeader("Authorization") String token, @PathVariable("id") Long id);
}