package ru.rsreu.festivalmanager.company.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.rsreu.festivalmanager.company.dto.CompanyAppRequestMsg;
import ru.rsreu.festivalmanager.company.event.CompanyEvents;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyKafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.company-application-request}")
    private String companyAppRequestTopic;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCompanyAppRequestEvent(CompanyEvents.CompanyAppRequestEvent event) {
        CompanyAppRequestMsg msg = CompanyAppRequestMsg.builder()
                .companyId(event.companyId())
                .companyName(event.companyName())
                .createdAt(event.createdAt())
                .build();

        String key = String.valueOf(event.companyId());

        kafkaTemplate.send(companyAppRequestTopic, key, msg).whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("Не удалось отправить сообщение в {} по причине: ", companyAppRequestTopic, exception);
            } else {
                log.info("Сообщение с ключом: {} отправлено в топик {} смещение {}",
                        result.getProducerRecord().key(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
