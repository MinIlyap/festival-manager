package ru.rsreu.festivalmanager.festival.event;

import lombok.Builder;

import java.time.OffsetDateTime;

public class FestivalEvents {
    @Builder
    public record FestivalAppRequestEvent(
            Long festivalLocationId,
            String festivalName,
            Long senderId,
            OffsetDateTime createdAt) {}
}
