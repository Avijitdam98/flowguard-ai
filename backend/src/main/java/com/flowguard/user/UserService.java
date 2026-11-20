package com.flowguard.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    UserDto getUserProfile(UUID userId);
    UserDto updateUserProfile(UUID userId, UpdateProfileRequest request);
    void changePassword(UUID userId, ChangePasswordRequest request);
    Page<UserDto> getAllUsers(Pageable pageable);
    UserDto assignUserRole(UUID userId, RoleType roleType);
    UserDto toggleUserStatus(UUID userId);
}

