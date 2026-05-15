package ru.rsreu.festivalmanager.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.event.entity.stand.Stand;

@Repository
public interface StandRepository extends JpaRepository<Stand, Long> {
}
