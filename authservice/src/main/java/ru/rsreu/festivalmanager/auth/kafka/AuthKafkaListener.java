package ru.rsreu.festivalmanager.auth.kafka;

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

    @RetryableTopic(
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            backOff = @BackOff(delay = 1000, multiplier = 2.0)
    )
    @KafkaListener(topics = "COMPANY_APPLICATION_RESPONSE_TOPIC", groupId = "auth-service-group")
    public void listenCompanyApplicationResponse(CompanyAppResponseMsg message) {
        log.info("Received message from COMPANY_APPLICATION_RESPONSE_TOPIC: {}", message);
        authService.processCompanyApplicationResponse(
                message.getCompanyId(),
                message.getStatus(),
                message.getComment()
        );
    }

    @DltHandler
    public void handleDltCompanyApplicationResponse(
            CompanyAppResponseMsg message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {

        log.error("DLT Handler: Message from topic '{}' failed completely. Exception: {}. Message Data: {}",
                topic, exceptionMessage, message);
    }
}
