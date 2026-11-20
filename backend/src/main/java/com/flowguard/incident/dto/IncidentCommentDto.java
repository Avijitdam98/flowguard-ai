package com.flowguard.incident.dto;

import com.flowguard.incident.IncidentComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentCommentDto {
    private UUID id;
    private UUID incidentId;
    private UUID userId;
    private String userName;
    private String commentText;
    private Instant createdAt;

    public static IncidentCommentDto fromEntity(IncidentComment comment) {
        if (comment == null) return null;
        return IncidentCommentDto.builder()
                .id(comment.getId())
                .incidentId(comment.getIncident() != null ? comment.getIncident().getId() : null)
                .userId(comment.getUserId())
                .userName(comment.getUserName())
                .commentText(comment.getCommentText())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}

