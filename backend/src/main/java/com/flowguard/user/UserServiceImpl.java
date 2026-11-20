package com.flowguard.user;

import com.flowguard.audit.AuditService;
import com.flowguard.common.BadRequestException;
import com.flowguard.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return UserDto.fromEntity(user);
    }

    @Override
    @Transactional
    public UserDto updateUserProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        User updated = userRepository.save(user);
        auditService.log(userId, updated.getEmail(), "UPDATE_PROFILE", "USER", userId.toString(), "User updated profile");
        return UserDto.fromEntity(updated);
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password does not match");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirmation do not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        auditService.log(userId, user.getEmail(), "CHANGE_PASSWORD", "USER", userId.toString(), "User changed password");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserDto::fromEntity);
    }

    @Override
    @Transactional
    public UserDto assignUserRole(UUID userId, RoleType roleType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleType));

        user.getRoles().add(role);
        User saved = userRepository.save(user);
        auditService.log(userId, user.getEmail(), "ASSIGN_ROLE", "USER", userId.toString(), "Role assigned: " + roleType);
        return UserDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public UserDto toggleUserStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setIsActive(!user.getIsActive());
        User saved = userRepository.save(user);
        auditService.log(userId, user.getEmail(), "TOGGLE_STATUS", "USER", userId.toString(), "Active status changed to: " + user.getIsActive());
        return UserDto.fromEntity(saved);
    }
}

