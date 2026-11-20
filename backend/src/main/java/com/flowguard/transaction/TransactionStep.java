package com.flowguard.transaction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "transaction_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(name = "service_name", length = 100)
    private String serviceName;

    @Column(name = "step_name", nullable = false, length = 150)
    private String stepName;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StepStatus status;

    @Builder.Default
    @Column(name = "duration_ms", nullable = false)
    private Integer durationMs = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "payload_snapshot", columnDefinition = "TEXT")
    private String payloadSnapshot;

    @Builder.Default
    @Column(name = "executed_at", nullable = false, updatable = false)
    private Instant executedAt = Instant.now();
}

