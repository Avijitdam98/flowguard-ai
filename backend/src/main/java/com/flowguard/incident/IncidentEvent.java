package com.flowguard.incident;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incident_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "event_description", nullable = false, columnDefinition = "TEXT")
    private String eventDescription;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "actor_name")
    private String actorName;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}

