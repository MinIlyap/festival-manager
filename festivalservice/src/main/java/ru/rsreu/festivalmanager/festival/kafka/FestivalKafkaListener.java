package ru.rsreu.festivalmanager.festival.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.festival-application-response}", groupId = "${kafka.groups.festival}")
    public void listenApplicationResponse(String jsonMessage) throws JsonProcessingException {
        if (jsonMessage.startsWith("\"") && jsonMessage.endsWith("\"")) {
            jsonMessage = objectMapper.readValue(jsonMessage, String.class);
        }

        FestivalAppResponseMsg message = objectMapper.readValue(jsonMessage, FestivalAppResponseMsg.class);
        log.info("Получен ответ по фестивалю ID: {}", message.getFestivalLocationId());

        festivalService.processApplicationResponse(
                message.getFestivalLocationId(),
                message.getStatus()
        );
    }
}