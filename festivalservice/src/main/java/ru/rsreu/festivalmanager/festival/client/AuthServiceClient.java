package ru.rsreu.festivalmanager.festival.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.festival.dto.AuthCredentialDto;

@FeignClient(name = "auth-service", url = "http://localhost:8082/auth")
public interface AuthServiceClient {

    @GetMapping("/search")
    AuthCredentialDto getUserByEmail(@RequestHeader("Authorization") String token, @RequestParam("email") String email);
}
