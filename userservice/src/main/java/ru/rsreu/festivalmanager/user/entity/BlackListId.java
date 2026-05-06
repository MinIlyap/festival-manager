package ru.rsreu.festivalmanager.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class BlackListId implements Serializable {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "curator_id", nullable = false)
    private Long curatorId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
