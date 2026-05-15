package ru.rsreu.festivalmanager.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "city_id", nullable = false)
    private Long city_id;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "passport_seria_hash")
    private String passportSeriaHash;

    @Column(name = "passport_number_hash")
    private String passportNumberHash;
}
