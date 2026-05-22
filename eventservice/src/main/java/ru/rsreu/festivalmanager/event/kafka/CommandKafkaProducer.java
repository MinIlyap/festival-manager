package ru.rsreu.festivalmanager.event.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.rsreu.festivalmanager.event.dto.CommandInviteEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC_NAME = "COMMAND_INVITE_TOPIC";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommandInvite(CommandInviteEvent event) {
        kafkaTemplate.send(TOPIC_NAME, event.getCommandId().toString(), event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Ошибка отправки приглашения в команду {} в топик: ", event.getCommandId(), exception);
                    } else {
                        log.info("Приглашение в команду {} успешно отправлено", event.getCommandId());
                    }
                });
    }
}
