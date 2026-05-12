package ru.rsreu.festivalmanager.auth.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.rsreu.festivalmanager.auth.data.AuthEvents;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCompanyAuthDataEvent(AuthEvents.CompanyAuthDataEvent event) {
        CompanyAuthMsg msg = CompanyAuthMsg.builder()
                .companyId(event.companyId())
                .login(event.login())
                .passwordHash(event.passwordHash())
                .comment(event.comment())
                .createdAt(event.createdAt())
                .build();

        String key = String.valueOf(event.companyId());

        kafkaTemplate.send("COMPANY_AUTH_TOPIC", key, msg)
                .whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("Couldn't send message to COMPANY_AUTH_TOPIC because: ", exception);
            } else {
                log.info("Message with key: {} sent to topic {} offset {}",
                        result.getProducerRecord().key(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
