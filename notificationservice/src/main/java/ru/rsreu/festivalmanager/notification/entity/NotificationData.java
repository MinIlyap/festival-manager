package ru.rsreu.festivalmanager.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "festival_location_id")
    private Long festivalLocationId;

    @Column(name = "festival_name")
    private String festivalName;

    @Column(name = "comment")
    private String comment;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "command_id")
    private Long commandId;

    @Column(name = "command_name")
    private String commandName;

    @Column(name = "related_user_id")
    private Long relatedUserId;

    @ManyToOne
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;
}
