package ru.rsreu.festivalmanager.festival.event;

import java.time.OffsetDateTime;

public class FestivalEvents {
    public record FestivalAppRequestEvent(Long festivalLocationId, String festivalName,
                                          Long senderId, OffsetDateTime createdAt) {}
}
