package com.flowguard.transaction;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
    private String transactionId;

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", nullable = false, length = 30)
    private TransactionStatus transactionStatus;

    @Builder.Default
    @Column(name = "start_time", nullable = false)
    private Instant startTime = Instant.now();

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "total_duration_ms")
    private Long totalDurationMs;

    @Column(name = "root_service_name", length = 100)
    private String rootServiceName;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("stepOrder ASC")
    private List<TransactionStep> steps = new ArrayList<>();
}

