package com.flowguard.team;

import com.flowguard.audit.AuditService;
import com.flowguard.common.BadRequestException;
import com.flowguard.common.ResourceNotFoundException;
import com.flowguard.user.User;
import com.flowguard.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public TeamDto createTeam(UUID creatorUserId, CreateTeamRequest request) {
        if (teamRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Team with slug '" + request.getSlug() + "' already exists");
        }
        if (teamRepository.existsByName(request.getName())) {
            throw new BadRequestException("Team with name '" + request.getName() + "' already exists");
        }

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", creatorUserId));

        Team team = Team.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .build();

        Team savedTeam = teamRepository.save(team);

        // Add creator as OWNER
        TeamMember member = TeamMember.builder()
                .id(new TeamMemberId(savedTeam.getId(), creator.getId()))
                .team(savedTeam)
                .user(creator)
                .role(TeamRole.OWNER)
                .joinedAt(Instant.now())
                .build();
        teamMemberRepository.save(member);

        auditService.log(creatorUserId, creator.getEmail(), "CREATE_TEAM", "TEAM", savedTeam.getId().toString(), "Created team " + savedTeam.getName());
        return TeamDto.fromEntity(savedTeam);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamDto getTeamById(UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
        return TeamDto.fromEntity(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamDto> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(TeamDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMemberDto> getTeamMembers(UUID teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team", "id", teamId);
        }
        return teamMemberRepository.findByTeamId(teamId).stream()
                .map(TeamMemberDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TeamMemberDto addTeamMember(UUID teamId, AddTeamMemberRequest request, UUID actorId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, user.getId())) {
            throw new BadRequestException("User is already a member of this team");
        }

        TeamMember member = TeamMember.builder()
                .id(new TeamMemberId(team.getId(), user.getId()))
                .team(team)
                .user(user)
                .role(request.getRole())
                .joinedAt(Instant.now())
                .build();

        TeamMember saved = teamMemberRepository.save(member);
        auditService.log(actorId, null, "ADD_TEAM_MEMBER", "TEAM", teamId.toString(), "Added member " + user.getEmail() + " as " + request.getRole());
        return TeamMemberDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public void removeTeamMember(UUID teamId, UUID userId, UUID actorId) {
        TeamMemberId id = new TeamMemberId(teamId, userId);
        TeamMember member = teamMemberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeamMember", "id", id));

        teamMemberRepository.delete(member);
        auditService.log(actorId, null, "REMOVE_TEAM_MEMBER", "TEAM", teamId.toString(), "Removed user " + userId + " from team");
    }
}

