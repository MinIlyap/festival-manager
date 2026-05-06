package ru.rsreu.festivalmanager.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "black_lists")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BlackList {

    @EmbeddedId
    private BlackListId blackListId;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;
}
