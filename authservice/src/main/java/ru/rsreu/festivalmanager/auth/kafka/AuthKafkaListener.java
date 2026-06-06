package ru.rsreu.festivalmanager.auth.kafka;

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
import ru.rsreu.festivalmanager.auth.service.AuthService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthKafkaListener {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @RetryableTopic(
            attempts = "${kafka.retry.attempts}",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            backOff = @BackOff(delay = 1000, multiplier = 2.0)
    )
    @KafkaListener(topics = "${kafka.topics.company-application-response}", groupId = "${kafka.groups.auth}")
    public void listenCompanyApplicationResponse(String jsonMessage) throws JsonProcessingException {

        while (jsonMessage.startsWith("\"") && jsonMessage.endsWith("\"")) {
            try {
                jsonMessage = objectMapper.readValue(jsonMessage, String.class);
            } catch (Exception e) {
                break;
            }
        }

        CompanyAppResponseMsg msg = objectMapper.readValue(jsonMessage, CompanyAppResponseMsg.class);

        log.info("Received message from COMPANY_APPLICATION_RESPONSE_TOPIC: {}", msg);
        authService.processCompanyApplicationResponse(
                msg.getCompanyId(),
                msg.getStatus(),
                msg.getComment()
        );
    }

    @DltHandler
    public void handleDltCompanyApplicationResponse(
            String jsonMessage,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {

        log.error("DLT Handler: Message from topic '{}' failed completely. Exception: {}. Message Data: {}",
                topic, exceptionMessage, jsonMessage);
    }
}
