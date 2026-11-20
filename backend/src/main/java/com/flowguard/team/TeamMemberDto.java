package com.flowguard.team;

import com.flowguard.user.UserDto;
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
public class TeamMemberDto {
    private UUID teamId;
    private UUID userId;
    private UserDto user;
    private TeamRole role;
    private Instant joinedAt;

    public static TeamMemberDto fromEntity(TeamMember member) {
        if (member == null) return null;
        return TeamMemberDto.builder()
                .teamId(member.getTeam().getId())
                .userId(member.getUser().getId())
                .user(UserDto.fromEntity(member.getUser()))
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}

