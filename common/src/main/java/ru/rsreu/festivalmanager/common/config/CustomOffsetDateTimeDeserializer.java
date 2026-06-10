package ru.rsreu.festivalmanager.common.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class CustomOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {
    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();

        try {
            return OffsetDateTime.parse(text);
        } catch (Exception e) {
            double timestamp = Double.parseDouble(text);
            long seconds = (long) timestamp;
            int nanos = (int) ((timestamp - seconds) * 1_000_000_000);

            return OffsetDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanos), ZoneOffset.UTC);
        }
    }
}
