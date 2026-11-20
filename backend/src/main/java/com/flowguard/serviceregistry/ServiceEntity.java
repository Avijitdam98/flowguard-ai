package com.flowguard.serviceregistry;

import com.flowguard.apimonitor.MonitorStatus;
import com.flowguard.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceEntity extends BaseEntity {

    @Column(name = "team_id")
    private UUID teamId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(nullable = false, length = 50)
    private String environment = "PRODUCTION";

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 20)
    private MonitorStatus currentStatus = MonitorStatus.HEALTHY;
}

