package ru.rsreu.festivalmanager.application.kafka;

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

    @RetryableTopic(
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            backOff = @BackOff(delay = 1000, multiplier = 2.0)
    )
    @KafkaListener(topics = "COMPANY_APPLICATION_REQUEST_TOPIC", groupId = "application-service-group")
    public void listenCompanyRequest(CompanyRequestMsg msg) {
        log.info("Получена заявка на компанию ID: {}", msg.getCompanyId());
        applicationService.processCompanyApplication(msg.getCompanyId(), msg.getCreatedAt());
    }

    @RetryableTopic(
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            backOff = @BackOff(delay = 1000, multiplier = 2.0)
    )
    @KafkaListener(topics = "FESTIVAL_APPLICATION_REQUEST_TOPIC", groupId = "application-service-group")
    public void listenFestivalRequest(FestivalRequestMsg msg) {
        log.info("Получена заявка на фестиваль ID: {}", msg.getFestivalLocationId());
        applicationService.processFestivalApplication(msg.getFestivalLocationId(), msg.getSenderId(), msg.getCreatedAt());
    }

    @DltHandler
    public void handleDlt(Object msg, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("СООБЩЕНИЕ ПОПАЛО В DLT! Топик: {}. Сообщение: {}", topic, msg);
    }
}