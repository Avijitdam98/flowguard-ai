package com.flowguard.auth;

import com.flowguard.audit.AuditService;
import com.flowguard.common.BadRequestException;
import com.flowguard.user.Role;
import com.flowguard.user.RoleRepository;
import com.flowguard.user.RoleType;
import com.flowguard.user.User;
import com.flowguard.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;
    private Role sampleRole;

    @BeforeEach
    void setUp() {
        sampleRole = Role.builder()
                .id(UUID.randomUUID())
                .name(RoleType.ENGINEER)
                .build();

        sampleUser = User.builder()
                .email("test@flowguard.ai")
                .passwordHash("encodedPassword")
                .firstName("Test")
                .lastName("Engineer")
                .isActive(true)
                .roles(Set.of(sampleRole))
                .build();
        sampleUser.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("test@flowguard.ai", "password123");
        UserPrincipal principal = UserPrincipal.create(sampleUser);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));
        when(tokenProvider.generateAccessToken(auth)).thenReturn("sample-access-token");
        when(tokenProvider.generateRefreshToken(sampleUser.getId(), sampleUser.getEmail())).thenReturn("sample-refresh-token");
        when(tokenProvider.getAccessTokenExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("sample-access-token", response.getAccessToken());
        assertEquals("sample-refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(sampleUser.getEmail(), response.getUser().getEmail());
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when credentials are invalid")
    void testLoginInvalidCredentials() {
        LoginRequest request = new LoginRequest("test@flowguard.ai", "wrongPassword");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Should register new user successfully")
    void testRegisterSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .email("new@flowguard.ai")
                .password("Password@123")
                .firstName("John")
                .lastName("Doe")
                .phone("+1234567890")
                .build();

        when(userRepository.existsByEmailIgnoreCase("new@flowguard.ai")).thenReturn(false);
        when(userRepository.count()).thenReturn(1L);
        when(roleRepository.findByName(RoleType.ENGINEER)).thenReturn(Optional.of(sampleRole));
        when(passwordEncoder.encode("Password@123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(tokenProvider.generateAccessToken(any())).thenReturn("new-access-token");
        when(tokenProvider.generateRefreshToken(any(), any())).thenReturn("new-refresh-token");
        when(tokenProvider.getAccessTokenExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration when email already exists")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@flowguard.ai")
                .password("Password@123")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userRepository.existsByEmailIgnoreCase("test@flowguard.ai")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
    }
}

