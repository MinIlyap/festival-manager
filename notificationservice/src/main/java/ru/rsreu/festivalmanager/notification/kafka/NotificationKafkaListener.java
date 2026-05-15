package ru.rsreu.festivalmanager.notification.kafka;

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
import ru.rsreu.festivalmanager.notification.dto.AdminNotificationMsg;
import ru.rsreu.festivalmanager.notification.dto.CommandInviteMsg;
import ru.rsreu.festivalmanager.notification.dto.CompanyResultMsg;
import ru.rsreu.festivalmanager.notification.dto.FestivalResponseMsg;
import ru.rsreu.festivalmanager.notification.service.NotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @RetryableTopic(backOff = @BackOff(delay = 1000, multiplier = 2.0),
            dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "COMPANY_AUTH_TOPIC", groupId = "notification-group")
    public void listenCompanyResult(String jsonMessage) throws JsonProcessingException {
        if (jsonMessage.startsWith("\"") && jsonMessage.endsWith("\"")) {
            jsonMessage = objectMapper.readValue(jsonMessage, String.class);
        }

        CompanyResultMsg msg = objectMapper.readValue(jsonMessage, CompanyResultMsg.class);
        notificationService.processCompanyResult(msg);
    }

    @RetryableTopic(backOff = @BackOff(delay = 1000, multiplier = 2.0),
            dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "FESTIVAL_APPLICATION_RESPONSE_TOPIC", groupId = "notification-group")
    public void listenFestivalResponse(String jsonMessage) throws JsonProcessingException {
        if (jsonMessage.startsWith("\"") && jsonMessage.endsWith("\"")) {
            jsonMessage = objectMapper.readValue(jsonMessage, String.class);
        }

        FestivalResponseMsg msg = objectMapper.readValue(jsonMessage, FestivalResponseMsg.class);
        notificationService.processFestivalResponse(msg);
    }

    @RetryableTopic(backOff = @BackOff(delay = 1000, multiplier = 2.0),
            dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "COMMAND_INVITE_TOPIC", groupId = "notification-group")
    public void listenTeamInvite(String jsonMessage) throws JsonProcessingException {
        if (jsonMessage.startsWith("\"") && jsonMessage.endsWith("\"")) {
            jsonMessage = objectMapper.readValue(jsonMessage, String.class);
        }

        CommandInviteMsg msg = objectMapper.readValue(jsonMessage, CommandInviteMsg.class);
        notificationService.processCommandInvite(msg);
    }

    @RetryableTopic(backOff = @BackOff(delay = 1000, multiplier = 2.0),
            dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "RESUBMIT_APPLICATION_TOPIC", groupId = "notification-group")
    public void listenAdminNotification(String jsonMessage) throws JsonProcessingException {
        if (jsonMessage.startsWith("\"") && jsonMessage.endsWith("\"")) {
            jsonMessage = objectMapper.readValue(jsonMessage, String.class);
        }

        AdminNotificationMsg msg = objectMapper.readValue(jsonMessage, AdminNotificationMsg.class);
        notificationService.processAdminNotification(msg);
    }

    @DltHandler
    public void handleDlt(Object msg, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Dead Letter Topic: Не удалось обработать сообщение. Топик: {}, Данные: {}", topic, msg);
    }
}