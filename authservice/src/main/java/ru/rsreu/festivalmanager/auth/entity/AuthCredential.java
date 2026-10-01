package ru.rsreu.festivalmanager.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auth_credentials")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(value = EnumType.STRING)
    @JsonProperty("access_type")
    @Column(name = "access_type")
    private AccessType accessType;

    @JsonProperty("user_id")
    @Column(name = "user_id")
    private Long userId;

    @JsonProperty("company_id")
    @Column(name = "company_id")
    private Long companyId;
}
