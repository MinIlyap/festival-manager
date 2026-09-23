package ru.rsreu.festivalmanager.festival.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.festival.entity.festival.FestivalLocation;
import ru.rsreu.festivalmanager.festival.entity.ticket.TicketType;
import ru.rsreu.festivalmanager.festival.entity.ticket.TicketTypeInstance;

import java.util.List;

@Repository
public interface TicketTypeInstanceRepository extends JpaRepository<TicketTypeInstance, Long> {
    List<TicketTypeInstance> findByFestivalLocation(FestivalLocation festivalLocation);

    List<TicketTypeInstance> findByFestivalLocation_Id(Long festivalLocationId);

    boolean existsByFestivalLocationAndTicketType(FestivalLocation location, TicketType ticketType);
}
