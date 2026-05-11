package ru.rsreu.festivalmanager.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.user.entity.BlackList;
import ru.rsreu.festivalmanager.user.entity.BlackListId;

import java.time.LocalDateTime;

@Repository
public interface BlackListRepository extends JpaRepository<BlackList, BlackListId> {

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BlackList b WHERE b.blackListId.user.id = :userId AND b.expiryDate > :now")
    boolean isUserBlacklistedNow(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BlackList b WHERE b.blackListId.user.phoneNumber = :phone AND b.expiryDate > :now")
    boolean isPhoneInBlackList(@Param("phone") String phone, @Param("now") LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BlackList b WHERE b.blackListId.user.passportSeriaHash = :seriaHash AND b.blackListId.user.passportNumberHash = :numberHash AND b.expiryDate > :now")
    boolean isPassportInBlackList(@Param("seriaHash") String seriaHash, @Param("numberHash") String numberHash, @Param("now") LocalDateTime now);
}
