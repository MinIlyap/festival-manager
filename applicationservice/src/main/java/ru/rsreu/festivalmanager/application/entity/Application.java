package ru.rsreu.festivalmanager.application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ApplicationType applicationType;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus applicationStatus;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "agent_id")
    private Long agentId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
