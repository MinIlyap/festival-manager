package ru.rsreu.festivalmanager.common.constants;

public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String COMPANY_APPLICATION_REQUEST  = "COMPANY_APPLICATION_REQUEST_TOPIC";
    public static final String COMPANY_APPLICATION_RESPONSE = "COMPANY_APPLICATION_RESPONSE_TOPIC";
    public static final String FESTIVAL_APPLICATION_REQUEST  = "FESTIVAL_APPLICATION_REQUEST_TOPIC";
    public static final String FESTIVAL_APPLICATION_RESPONSE = "FESTIVAL_APPLICATION_RESPONSE_TOPIC";
    public static final String COMPANY_AUTH             = "COMPANY_AUTH_TOPIC";
    public static final String COMMAND_INVITE           = "COMMAND_INVITE_TOPIC";
    public static final String RESUBMIT_APPLICATION     = "RESUBMIT_APPLICATION_TOPIC";
}
