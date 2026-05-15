package ru.rsreu.festivalmanager.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.event.entity.event.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
}
