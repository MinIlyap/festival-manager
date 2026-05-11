package ru.rsreu.festivalmanager.auth.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.rsreu.festivalmanager.auth.service.AuthService;

@Component
@RequiredArgsConstructor
public class AuthKafkaListener {

    private final AuthService authService;

    @KafkaListener(topics = "COMPANY_APPLICATION_RESPONSE_TOPIC", groupId = "auth-service-group")
    public void listenCompanyApplicationResponse(CompanyAppResponseMsg message) {
        authService.processCompanyApplicationResponse(
                message.getCompanyId(),
                message.getStatus(),
                message.getComment()
        );
    }
}
