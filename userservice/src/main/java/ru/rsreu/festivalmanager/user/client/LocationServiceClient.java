package ru.rsreu.festivalmanager.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "location-service", url = "http://localhost:8084/location")
@Component
public interface LocationServiceClient {

    @GetMapping("/city/check/{id}")
    boolean existsCityById(@RequestHeader("Authorization") String token, @PathVariable Long id);
}
