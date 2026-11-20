package com.flowguard.user;

import com.flowguard.auth.UserPrincipal;
import com.flowguard.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile and role administration endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUserProfile(@AuthenticationPrincipal UserPrincipal principal) {
        UserDto profile = userService.getUserProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Profile retrieved", profile));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserDto updated = userService.updateUserProfile(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", updated));
    }

    @PutMapping("/password")
    @Operation(summary = "Change account password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "List all platform users (Admin only)")
    public ResponseEntity<ApiResponse<Page<UserDto>>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.ok("Users retrieved", users));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Assign a role to a user (Super Admin only)")
    public ResponseEntity<ApiResponse<UserDto>> assignRole(
            @PathVariable UUID id,
            @RequestParam RoleType roleType) {
        UserDto updated = userService.assignUserRole(id, roleType);
        return ResponseEntity.ok(ApiResponse.ok("Role assigned successfully", updated));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Toggle user active status (Admin only)")
    public ResponseEntity<ApiResponse<UserDto>> toggleStatus(@PathVariable UUID id) {
        UserDto updated = userService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.ok("User status updated", updated));
    }
}

