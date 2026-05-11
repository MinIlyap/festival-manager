package ru.rsreu.festivalmanager.auth.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCompanyApplicationRequest(Long companyId, String companyName) {
        var msg = CompanyAppRequestMsg.builder()
                .companyId(companyId)
                .companyName(companyName)
                .createdAt(OffsetDateTime.now())
                .build();
        kafkaTemplate.send("COMPANY_APPLICATION_REQUEST_TOPIC", msg);
    }

    public void sendCompanyAuthData(Long companyId, String login, String rawPassword, String comment) {
        var msg = CompanyAuthMsg.builder()
                .companyId(companyId)
                .login(login)
                .passwordHash(rawPassword)
                .comment(comment)
                .createdAt(OffsetDateTime.now())
                .build();
        kafkaTemplate.send("COMPANY_AUTH_TOPIC", msg);
    }
}
