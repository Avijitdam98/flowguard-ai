package com.flowguard.alert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Builder.Default
    @Column(nullable = false, length = 50)
    private String channel = "IN_APP";

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Builder.Default
    @Column(nullable = false, length = 30)
    private String status = "SENT";

    @Builder.Default
    @Column(name = "sent_at", nullable = false)
    private Instant sentAt = Instant.now();
}

