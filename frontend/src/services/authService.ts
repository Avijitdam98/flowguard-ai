import api from './api';
import { AuthResponse, ForgotPasswordRequest, LoginRequest, RegisterRequest, ResetPasswordRequest, User } from '../types/auth';

export const authService = {
  async login(payload: LoginRequest): Promise<AuthResponse> {
    const response = await api.post('/auth/login', payload);
    return response.data.data;
  },

  async register(payload: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post('/auth/register', payload);
    return response.data.data;
  },

  async refreshToken(refreshToken: string): Promise<AuthResponse> {
    const response = await api.post('/auth/refresh', { refreshToken });
    return response.data.data;
  },

  async logout(): Promise<void> {
    try {
      await api.post('/auth/logout');
    } finally {
      localStorage.removeItem('flowguard_access_token');
      localStorage.removeItem('flowguard_refresh_token');
      localStorage.removeItem('flowguard_user');
    }
  },

  async forgotPassword(payload: ForgotPasswordRequest): Promise<void> {
    await api.post('/auth/forgot-password', payload);
  },

  async resetPassword(payload: ResetPasswordRequest): Promise<void> {
    await api.post('/auth/reset-password', payload);
  },

  async getCurrentUser(): Promise<User> {
    const response = await api.get('/auth/me');
    return response.data.data;
  },
};

