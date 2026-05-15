package ru.rsreu.festivalmanager.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.auth.dto.CompanyInfoDto;
import ru.rsreu.festivalmanager.auth.dto.CompanyRegistrationRequest;

@FeignClient(name = "company-service", url = "http://localhost:8083/company")
public interface CompanyServiceClient {

    @PostMapping
    Long createInactiveCompany(@RequestHeader("Authorization") String token, @RequestBody CompanyRegistrationRequest request);

    @PutMapping("/activate/{id}")
    void activateCompany(@RequestHeader("Authorization") String token, @PathVariable Long id);

    @PutMapping("/email/{id}")
    void updateCompanyEmail(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestParam("newEmail") String newEmail);

    @GetMapping("/{id}")
    CompanyInfoDto getCompanyById(@RequestHeader("Authorization") String token, @PathVariable Long id);
}
