package ru.rsreu.festivalmanager.application.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.rsreu.festivalmanager.application.dto.AppResponseMsg;
import ru.rsreu.festivalmanager.application.dto.AppResubmittedMsg;
import ru.rsreu.festivalmanager.application.entity.ApplicationType;
import ru.rsreu.festivalmanager.application.event.ApplicationEvents;
import ru.rsreu.festivalmanager.application.event.ApplicationEvents.AppResponseEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAppResponseEvent(AppResponseEvent event) {

        AppResponseMsg msg = AppResponseMsg.builder()
                .status(event.status().name())
                .comment(event.comment())
                .updatedAt(event.updatedAt())
                .companyId(event.companyId())
                .festivalLocationId(event.festivalLocationId())
                .build();

        String topic = event.type() == ApplicationType.COMPANY_APPLICATION
                ? "COMPANY_APPLICATION_RESPONSE_TOPIC"
                : "FESTIVAL_APPLICATION_RESPONSE_TOPIC";

        String key = event.type() == ApplicationType.COMPANY_APPLICATION
                ? String.valueOf(event.companyId())
                : String.valueOf(event.festivalLocationId());

        kafkaTemplate.send(topic, key, msg).whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("КРИТИЧЕСКАЯ ОШИБКА: Не удалось отправить ответ по заявке в топик {}. Причина: ", topic, exception);
            } else {
                log.info("Успешно отправлен статус {} в топик {} (offset: {})", msg.getStatus(), topic, result.getRecordMetadata().offset());
            }
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAppResubmittedEvent(ApplicationEvents.AppResubmittedEvent event) {

        AppResubmittedMsg msg = AppResubmittedMsg.builder()
                .agentId(event.agentId())
                .type(event.type())
                .entityId(event.entityId())
                .updatedAt(event.updatedAt())
                .build();

        String topic = "RESUBMIT_APPLICATION_TOPIC";
        String key = String.valueOf(event.agentId());

        kafkaTemplate.send(topic, key, msg).whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("КРИТИЧЕСКАЯ ОШИБКА: Не удалось отправить уведомление о переподаче в топик {}. Причина: ", topic, exception);
            } else {
                log.info("Успешно отправлено уведомление админу {} в топик {} (offset: {})", msg.getAgentId(), topic, result.getRecordMetadata().offset());
            }
        });
    }
}
