package ru.rsreu.festivalmanager.event.entity.registration;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "command_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandMember {
    @EmbeddedId
    private CommandMemberId commandMemberId;
}
