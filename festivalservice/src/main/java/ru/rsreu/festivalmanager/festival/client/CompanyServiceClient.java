package ru.rsreu.festivalmanager.festival.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.festival.dto.festival.CompanyCreateDto;
import ru.rsreu.festivalmanager.festival.dto.festival.CompanyDto;

@FeignClient(name = "company-service", url = "${services.company.url}")
public interface CompanyServiceClient {
    @GetMapping("/search")
    CompanyDto getCompanyByTin(@RequestHeader("Authorization") String token, @RequestParam("tin") String tin);

    @PostMapping
    Long createInactiveCompany(@RequestHeader("Authorization") String token, @RequestBody CompanyCreateDto request);

    @GetMapping("/{id}")
    CompanyDto getCompanyById(@RequestHeader("Authorization") String token, @PathVariable Long id);
}
