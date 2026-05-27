package ru.rsreu.festivalmanager.festival.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.festival.entity.ticket.Ticket;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Ticket t WHERE t.ticketTypeInstance.festivalLocation.festivalInstance.id = :instanceId")
    Integer sumSoldTicketsByFestivalInstanceId(@Param("instanceId") Long instanceId);

    List<Ticket> findAllByUserId(Long userId);

    boolean existsByUserIdAndTicketTypeInstance_FestivalLocation_IdAndExpiryDateAfter(
            Long userId,
            Long festivalLocationId,
            LocalDateTime date
    );

    boolean existsByTicketTypeInstanceId(Long instanceId);

    boolean existsByUserIdAndTicketTypeInstance_FestivalLocation_Id(Long userId, Long festivalLocationId);

    @Query("SELECT DISTINCT t.userId FROM Ticket t WHERE t.ticketTypeInstance.festivalLocation.id = :festivalLocationId")
    List<Long> findDistinctUserIdsByFestivalLocationId(@Param("festivalLocationId") Long festivalLocationId);
}
