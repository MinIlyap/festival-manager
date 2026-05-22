package ru.rsreu.festivalmanager.festival.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.festival.entity.ticket.TicketType;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    boolean existsByNameIgnoreCase(String name);
}
