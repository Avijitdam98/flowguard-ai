package com.flowguard.team;

import java.util.List;
import java.util.UUID;

public interface TeamService {
    TeamDto createTeam(UUID creatorUserId, CreateTeamRequest request);
    TeamDto getTeamById(UUID teamId);
    List<TeamDto> getAllTeams();
    List<TeamMemberDto> getTeamMembers(UUID teamId);
    TeamMemberDto addTeamMember(UUID teamId, AddTeamMemberRequest request, UUID actorId);
    void removeTeamMember(UUID teamId, UUID userId, UUID actorId);
}

