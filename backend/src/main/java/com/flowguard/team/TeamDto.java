package com.flowguard.team;

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
public class TeamDto {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private Instant createdAt;

    public static TeamDto fromEntity(Team team) {
        if (team == null) return null;
        return TeamDto.builder()
                .id(team.getId())
                .name(team.getName())
                .slug(team.getSlug())
                .description(team.getDescription())
                .createdAt(team.getCreatedAt())
                .build();
    }
}

