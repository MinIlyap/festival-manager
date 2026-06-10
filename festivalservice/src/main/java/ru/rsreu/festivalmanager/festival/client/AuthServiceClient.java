package ru.rsreu.festivalmanager.festival.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.festival.dto.festival.AuthCredentialDto;

import java.util.List;

@FeignClient(name = "auth-service", url = "${services.auth.url}")
public interface AuthServiceClient {
    @PutMapping("/roles/curator")
    void assignCuratorRoles(@RequestHeader("Authorization") String token, @RequestBody List<Long> userIds);

    @GetMapping("/search")
    AuthCredentialDto getUserByEmail(@RequestHeader("Authorization") String token, @RequestParam("email") String email);

    @GetMapping("/users/{userId}")
    AuthCredentialDto getCredentialByUserId(@RequestHeader("Authorization") String token, @PathVariable Long userId);
}
