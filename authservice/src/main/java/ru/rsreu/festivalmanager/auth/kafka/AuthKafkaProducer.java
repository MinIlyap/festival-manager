package ru.rsreu.festivalmanager.auth.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.rsreu.festivalmanager.auth.event.AuthEvents;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthKafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.company-auth}")
    private String companyAuthTopic;

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

        kafkaTemplate.send(companyAuthTopic, key, msg)
                .whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("Не удалось отправить сообщение в {} потому что: ", companyAuthTopic, exception);
            } else {
                log.info("Сообщение с ключом: {} отправлено в топик {} смещение {}",
                        result.getProducerRecord().key(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
