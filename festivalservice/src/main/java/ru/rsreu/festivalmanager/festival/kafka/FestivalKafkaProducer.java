package ru.rsreu.festivalmanager.festival.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.rsreu.festivalmanager.festival.dto.FestivalAppRequestMsg;
import ru.rsreu.festivalmanager.festival.event.FestivalEvents;

@Slf4j
@Component
@RequiredArgsConstructor
public class FestivalKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFestivalAppRequestEvent(FestivalEvents.FestivalAppRequestEvent event) {
        FestivalAppRequestMsg msg = FestivalAppRequestMsg.builder()
                .festivalLocationId(event.festivalLocationId())
                .festivalName(event.festivalName())
                .senderId(event.senderId())
                .build();

        String key = String.valueOf(event.festivalLocationId());

        kafkaTemplate.send("FESTIVAL_APPLICATION_REQUEST_TOPIC", key, msg).whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("Ошибка отправки заявки на фестиваль в топик: ", exception);
            } else {
                log.info("Заявка на фестиваль {} успешно отправлена", event.festivalLocationId());
            }
        });
    }
}
