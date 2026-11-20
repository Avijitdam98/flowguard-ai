package com.flowguard.auth;

import com.flowguard.common.ApiResponse;
import com.flowguard.user.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication, registration, token refresh, and password recovery endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and receive JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Registration successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh expired access token using valid refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user and invalidate session")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String token) {
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Initiate password reset flow")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("If an account exists with that email, a password reset link has been dispatched.", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Complete password reset using token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password has been reset successfully. Please log in with your new password.", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated user details")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        UserDto user = authService.getCurrentUser(principal);
        return ResponseEntity.ok(ApiResponse.ok("Current user retrieved", user));
    }
}

