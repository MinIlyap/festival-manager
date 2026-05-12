package ru.rsreu.festivalmanager.company.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCompanyAppRequestEvent(CompanyEvents.CompanyAppRequestEvent event) {
        CompanyAppRequestMsg msg = CompanyAppRequestMsg.builder()
                .companyId(event.companyId())
                .companyName(event.companyName())
                .createdAt(event.createdAt())
                .build();

        String key = String.valueOf(event.companyId());

        kafkaTemplate.send("COMPANY_APPLICATION_REQUEST_TOPIC", key, msg).whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("Couldn't send message to COMPANY_APPLICATION_REQUEST_TOPIC because: ", exception);
            } else {
                log.info("Message with key: {} sent to topic {} offset {}",
                        result.getProducerRecord().key(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
