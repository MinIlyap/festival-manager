package ru.rsreu.festivalmanager.file.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "festival-service", url = "http://localhost:8085/internal/access/festivals/")
public interface FestivalServiceClient {
    @GetMapping("{entityType}/{entityId}")
    boolean checkAccess(@PathVariable String entityType,
                        @PathVariable Long entityId,
                        @RequestParam("companyId") Long companyId);
}