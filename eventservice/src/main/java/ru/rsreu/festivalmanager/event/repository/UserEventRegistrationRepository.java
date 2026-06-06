package ru.rsreu.festivalmanager.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.event.entity.registration.UserEventRegistration;
import ru.rsreu.festivalmanager.event.entity.registration.UserEventRegistrationId;

import java.util.List;

@Repository
public interface UserEventRegistrationRepository extends JpaRepository<UserEventRegistration, UserEventRegistrationId> {
    long countByUserEventRegistrationId_FestivalEvent_Id(Long festivalEventId);

    boolean existsByUserEventRegistrationId_FestivalEvent_IdAndUserEventRegistrationId_UserId(Long festivalEventId, Long userId);

    List<UserEventRegistration> findAllByUserEventRegistrationId_FestivalEvent_Id(Long festivalEventId);

    @Query("SELECT r.userEventRegistrationId.userId FROM UserEventRegistration r " +
            "WHERE r.userEventRegistrationId.festivalEvent.id = :eventId " +
            "AND r.userEventRegistrationId.userId NOT IN " +
            "(SELECT cm.commandMemberId.userId FROM CommandMember cm WHERE cm.commandMemberId.command.festivalEvent.id = :eventId)")
    List<Long> findUserIdsWithoutCommand(@Param("eventId") Long eventId);

    List<UserEventRegistration> findAllByUserEventRegistrationId_UserId(Long userId);
}