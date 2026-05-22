package ru.rsreu.festivalmanager.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.file.entity.EntityType;
import ru.rsreu.festivalmanager.file.entity.UserFile;

import java.util.Optional;

@Repository
public interface UserFileRepository extends JpaRepository<UserFile, Long> {
    Optional<UserFile> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);
}
