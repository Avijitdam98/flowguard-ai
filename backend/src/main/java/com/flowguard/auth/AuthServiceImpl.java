package com.flowguard.auth;

import com.flowguard.audit.AuditService;
import com.flowguard.common.BadRequestException;
import com.flowguard.common.ResourceNotFoundException;
import com.flowguard.common.UnauthorizedException;
import com.flowguard.user.Role;
import com.flowguard.user.RoleRepository;
import com.flowguard.user.RoleType;
import com.flowguard.user.User;
import com.flowguard.user.UserDto;
import com.flowguard.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String cleanEmail = request.getEmail() != null ? request.getEmail().trim() : "";
        log.info("Attempting login for user: {}", cleanEmail);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(cleanEmail, request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            log.warn("Login failed: invalid credentials for email {}", cleanEmail);
            throw new BadCredentialsException("Invalid email or password");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Your account is deactivated. Please contact an administrator.");
        }

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        auditService.log(user.getId(), user.getEmail(), "LOGIN", "AUTH", user.getId().toString(), "User successfully logged in");

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpirationMs() / 1000)
                .user(UserDto.fromEntity(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String cleanEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        log.info("Registering new user with email: {}", cleanEmail);

        if (userRepository.existsByEmailIgnoreCase(cleanEmail)) {
            throw new BadRequestException("An account with email '" + cleanEmail + "' already exists");
        }

        Set<Role> roles = new HashSet<>();
        // If this is the very first user on the platform, grant SUPER_ADMIN, otherwise ENGINEER
        if (userRepository.count() == 0) {
            Role superAdminRole = roleRepository.findByName(RoleType.SUPER_ADMIN)
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name(RoleType.SUPER_ADMIN)
                            .description("Super Administrator")
                            .build()));
            roles.add(superAdminRole);
        } else {
            Role engineerRole = roleRepository.findByName(RoleType.ENGINEER)
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name(RoleType.ENGINEER)
                            .description("Software Engineer")
                            .build()));
            roles.add(engineerRole);
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .isActive(true)
                .emailVerified(true) // Auto-verified for SaaS demo convenience
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        UserPrincipal principal = UserPrincipal.create(savedUser);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        String accessToken = tokenProvider.generateAccessToken(auth);
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getId(), savedUser.getEmail());

        auditService.log(savedUser.getId(), savedUser.getEmail(), "REGISTER", "AUTH", savedUser.getId().toString(), "User registered account");

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpirationMs() / 1000)
                .user(UserDto.fromEntity(savedUser))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        if (!tokenProvider.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String tokenType = tokenProvider.getTokenType(token);
        if (!"REFRESH".equals(tokenType)) {
            throw new BadRequestException("Provided token is not a refresh token");
        }

        UUID userId = tokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Account is inactive");
        }

        UserPrincipal principal = UserPrincipal.create(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        String newAccessToken = tokenProvider.generateAccessToken(auth);
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpirationMs() / 1000)
                .user(UserDto.fromEntity(user))
                .build();
    }

    @Override
    public void logout(String bearerToken) {
        // In a stateless JWT setup, client deletes token.
        // Optional Redis blacklist token can be added in Phase 6.
        SecurityContextHolder.clearContext();
        log.info("User logged out successfully");
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail().toLowerCase().trim()).ifPresent(user -> {
            log.info("Password reset requested for user: {}", user.getEmail());
            auditService.log(user.getId(), user.getEmail(), "FORGOT_PASSWORD_REQUEST", "AUTH", user.getId().toString(), "Password reset requested");
        });
        // We always return 200 to prevent user enumeration attacks
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Validate reset token
        if (!tokenProvider.validateToken(request.getToken())) {
            throw new BadRequestException("Password reset link is invalid or has expired");
        }

        UUID userId = tokenProvider.getUserIdFromToken(request.getToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditService.log(user.getId(), user.getEmail(), "RESET_PASSWORD_COMPLETE", "AUTH", user.getId().toString(), "Password successfully reset");
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUser(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));
        return UserDto.fromEntity(user);
    }
}

