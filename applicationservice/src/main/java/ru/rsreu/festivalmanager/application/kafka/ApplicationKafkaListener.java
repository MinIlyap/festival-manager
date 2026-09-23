package ru.rsreu.festivalmanager.application.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.rsreu.festivalmanager.application.dto.CompanyRequestMsg;
import ru.rsreu.festivalmanager.application.dto.FestivalRequestMsg;
import ru.rsreu.festivalmanager.application.service.ApplicationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationKafkaListener {
    private final ApplicationService applicationService;
    private final ObjectMapper objectMapper;

    @RetryableTopic(
            attempts = "${kafka.retry.attempts}",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            backOff = @BackOff(delay = 1000, multiplier = 2.0)
    )
    @KafkaListener(topics = "${kafka.topics.company-application-request}", groupId = "${kafka.groups.application}")
    public void listenCompanyRequest(String jsonMessage) throws JsonProcessingException {
        if (jsonMessage.startsWith("\"") && jsonMessage.endsWith("\"")) {
            jsonMessage = objectMapper.readValue(jsonMessage, String.class);
        }

        CompanyRequestMsg msg = objectMapper.readValue(jsonMessage, CompanyRequestMsg.class);
        log.info("Получена заявка на компанию ID: {}", msg.getCompanyId());
        applicationService.processCompanyApplication(msg.getCompanyId(), msg.getCreatedAt());
    }

    @RetryableTopic(
            attempts = "${kafka.retry.attempts}",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            backOff = @BackOff(delay = 1000, multiplier = 2.0)
    )
    @KafkaListener(topics = "${kafka.topics.festival-application-request}", groupId = "${kafka.groups.application}")
    public void listenFestivalRequest(String jsonMessage) throws JsonProcessingException {
        if (jsonMessage.startsWith("\"") && jsonMessage.endsWith("\"")) {
            jsonMessage = objectMapper.readValue(jsonMessage, String.class);
        }

        FestivalRequestMsg msg = objectMapper.readValue(jsonMessage, FestivalRequestMsg.class);
        log.info("Получена заявка на фестиваль ID: {}", msg.getFestivalLocationId());
        applicationService.processFestivalApplication(msg.getFestivalLocationId(), msg.getSenderId(), msg.getCreatedAt());
    }

    @DltHandler
    public void handleDlt(String jsonMessage, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("СООБЩЕНИЕ ПОПАЛО В DLT! Топик: {}. Сообщение: {}", topic, jsonMessage);
    }
}
