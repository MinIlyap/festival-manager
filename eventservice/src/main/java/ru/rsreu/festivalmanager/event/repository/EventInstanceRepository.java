package ru.rsreu.festivalmanager.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.event.entity.event.EventInstance;
import ru.rsreu.festivalmanager.event.entity.event.EventType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface EventInstanceRepository extends JpaRepository<EventInstance, Long> {
    @Query("SELECT ei FROM EventInstance ei " +
            "WHERE ei.event.id = :eventId " +
            "AND ei.companyId = :companyId " +
            "AND ei.eventType = :type " +
            "AND ei.dateStart = :dateStart " +
            "AND ei.timeStart = :timeStart " +
            "AND ei.isCommand = :isCommand " +
            "AND ei.maxNumberPeople = :maxNumberPeople " +
            "AND (ei.description = :description OR (ei.description IS NULL AND :description IS NULL)) " +
            "AND (ei.approximateDuration = :approximateDuration OR (ei.approximateDuration IS NULL AND :approximateDuration IS NULL)) " +
            "AND (ei.maxPeopleCommand = :maxPeopleCommand OR (ei.maxPeopleCommand IS NULL AND :maxPeopleCommand IS NULL))")

    Optional<EventInstance> findIdenticalInstance(
            @Param("eventId") Long eventId,
            @Param("companyId") Long companyId,
            @Param("type") EventType type,
            @Param("dateStart") LocalDate dateStart,
            @Param("timeStart") LocalTime timeStart,
            @Param("isCommand") Boolean isCommand,
            @Param("maxNumberPeople") Integer maxNumberPeople,
            @Param("description") String description,
            @Param("approximateDuration") Integer approximateDuration,
            @Param("maxPeopleCommand") Integer maxPeopleCommand
    );
}
