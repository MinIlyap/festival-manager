package ru.rsreu.festivalmanager.event.entity.registration;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandMemberId implements Serializable {
    @ManyToOne
    @JoinColumn(name = "command_id", nullable = false)
    private Command command;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}
