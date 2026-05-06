package ru.rsreu.festivalmanager.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auth_credentials")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "company_id")
    private Long companyId;
}
