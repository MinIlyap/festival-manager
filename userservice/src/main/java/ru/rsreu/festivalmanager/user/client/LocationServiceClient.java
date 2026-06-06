package ru.rsreu.festivalmanager.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.rsreu.festivalmanager.user.dto.CityNameResponse;

@FeignClient(name = "location-service", url = "${services.location.url}")
public interface LocationServiceClient {
    @GetMapping("/city/check/{id}")
    boolean existsCityById(@RequestHeader("Authorization") String token, @PathVariable Long id);

    @GetMapping("/city/{id}")
    CityNameResponse getCityById(@RequestHeader("Authorization") String token, @PathVariable Long id);
}
