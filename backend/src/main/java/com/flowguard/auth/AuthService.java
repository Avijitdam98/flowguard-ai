package com.flowguard.auth;

import com.flowguard.user.UserDto;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String bearerToken);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    UserDto getCurrentUser(UserPrincipal principal);
}

