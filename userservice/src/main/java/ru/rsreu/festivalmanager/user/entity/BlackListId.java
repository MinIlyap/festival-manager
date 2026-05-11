package ru.rsreu.festivalmanager.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne
    @JoinColumn(name = "curator_id", nullable = false)
    private AppUser curator;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
