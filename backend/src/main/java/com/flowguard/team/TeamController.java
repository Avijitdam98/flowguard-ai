package com.flowguard.team;

import com.flowguard.auth.UserPrincipal;
import com.flowguard.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "Team Management", description = "Team workspace and membership administration endpoints")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Create a new team workspace")
    public ResponseEntity<ApiResponse<TeamDto>> createTeam(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateTeamRequest request) {
        TeamDto created = teamService.createTeam(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Team created successfully", created));
    }

    @GetMapping
    @Operation(summary = "List all team workspaces")
    public ResponseEntity<ApiResponse<List<TeamDto>>> getAllTeams() {
        List<TeamDto> teams = teamService.getAllTeams();
        return ResponseEntity.ok(ApiResponse.ok("Teams retrieved", teams));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team workspace by ID")
    public ResponseEntity<ApiResponse<TeamDto>> getTeamById(@PathVariable UUID id) {
        TeamDto team = teamService.getTeamById(id);
        return ResponseEntity.ok(ApiResponse.ok("Team retrieved", team));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "List members of a team")
    public ResponseEntity<ApiResponse<List<TeamMemberDto>>> getTeamMembers(@PathVariable UUID id) {
        List<TeamMemberDto> members = teamService.getTeamMembers(id);
        return ResponseEntity.ok(ApiResponse.ok("Team members retrieved", members));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Add a member to a team")
    public ResponseEntity<ApiResponse<TeamMemberDto>> addTeamMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddTeamMemberRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        TeamMemberDto member = teamService.addTeamMember(id, request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Member added successfully", member));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Remove a member from a team")
    public ResponseEntity<ApiResponse<Void>> removeTeamMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        teamService.removeTeamMember(id, userId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Member removed successfully", null));
    }
}

