package ru.rsreu.festivalmanager.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_social_accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String provider;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "email")
    private String email;
}
