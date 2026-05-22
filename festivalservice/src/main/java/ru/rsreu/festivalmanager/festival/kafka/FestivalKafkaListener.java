package ru.rsreu.festivalmanager.festival.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.rsreu.festivalmanager.festival.dto.FestivalAppResponseMsg;
import ru.rsreu.festivalmanager.festival.service.FestivalService;

@Slf4j
@Component
@RequiredArgsConstructor
public class FestivalKafkaListener {

    private final FestivalService festivalService;

    @KafkaListener(topics = "FESTIVAL_APPLICATION_RESPONSE_TOPIC", groupId = "festival-service-group")
    public void listenApplicationResponse(FestivalAppResponseMsg message) {
        log.info("Получен ответ по фестивалю ID: {}", message.getFestivalLocationId());

        festivalService.processApplicationResponse(
                message.getFestivalLocationId(),
                message.getStatus()
        );
    }
}