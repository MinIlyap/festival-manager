package ru.rsreu.festivalmanager.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.event.entity.registration.CommandMember;
import ru.rsreu.festivalmanager.event.entity.registration.CommandMemberId;

import java.util.List;

@Repository
public interface CommandMemberRepository extends JpaRepository<CommandMember, CommandMemberId> {
    long countByCommandMemberId_Command_id(Long commandId);

    boolean existsByCommandMemberId_UserIdAndCommandMemberId_Command_FestivalEvent_id(Long userId, Long festivalEventId);

    boolean existsByCommandMemberId_UserIdAndCommandMemberId_Command_Id(Long userId, Long commandId);

    List<CommandMember> findAllByCommandMemberId_Command_Id(Long commandId);

    void deleteByCommandMemberId_UserIdAndCommandMemberId_Command_Id(Long userId, Long commandId);
}
