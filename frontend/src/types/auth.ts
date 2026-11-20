export type RoleType = 'SUPER_ADMIN' | 'ADMIN' | 'ENGINEER' | 'VIEWER';

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  isActive: boolean;
  emailVerified: boolean;
  roles: RoleType[];
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

