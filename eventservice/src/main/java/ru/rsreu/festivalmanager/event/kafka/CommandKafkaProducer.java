package ru.rsreu.festivalmanager.event.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.rsreu.festivalmanager.event.dto.CommandInviteEvent;

@Component
@RequiredArgsConstructor
public class CommandKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC_NAME = "COMMAND_INVITE_TOPIC";

    public void sendInvite(CommandInviteEvent event) {
        kafkaTemplate.send(TOPIC_NAME, event);
    }
}
