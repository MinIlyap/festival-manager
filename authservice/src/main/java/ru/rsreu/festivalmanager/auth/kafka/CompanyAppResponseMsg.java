package ru.rsreu.festivalmanager.auth.kafka;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CompanyAppResponseMsg {
    private Long companyId;
    private String status;
    private String comment;
    private LocalDateTime updatedAt;
}