package ru.rsreu.festivalmanager.file.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "event-service", url = "${services.event.url}")
public interface EventServiceClient {
    @GetMapping("{entityType}/{entityId}")
    boolean checkAccess(@RequestHeader("Authorization") String token,
                        @PathVariable String entityType,
                        @PathVariable Long entityId,
                        @RequestParam("companyId") Long companyId);
}
