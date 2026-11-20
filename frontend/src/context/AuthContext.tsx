import React, { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react';
import { authService } from '../services/authService';
import { AuthResponse, LoginRequest, RegisterRequest, RoleType, User } from '../types/auth';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<AuthResponse>;
  register: (payload: RegisterRequest) => Promise<AuthResponse>;
  logout: () => Promise<void>;
  hasRole: (role: RoleType | RoleType[]) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const cached = localStorage.getItem('flowguard_user');
    if (cached) {
      try {
        return JSON.parse(cached);
      } catch {
        return null;
      }
    }
    return null;
  });

  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem('flowguard_access_token');
  });

  const [isLoading, setIsLoading] = useState<boolean>(true);

  // Revalidate session on initial mount
  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('flowguard_access_token');
      if (storedToken) {
        try {
          const currentUser = await authService.getCurrentUser();
          setUser(currentUser);
          localStorage.setItem('flowguard_user', JSON.stringify(currentUser));
        } catch {
          // Token invalid or expired
          localStorage.removeItem('flowguard_access_token');
          localStorage.removeItem('flowguard_refresh_token');
          localStorage.removeItem('flowguard_user');
          setUser(null);
          setToken(null);
        }
      }
      setIsLoading(false);
    };

    initAuth();
  }, []);

  const login = useCallback(async (credentials: LoginRequest): Promise<AuthResponse> => {
    const data = await authService.login(credentials);
    localStorage.setItem('flowguard_access_token', data.accessToken);
    localStorage.setItem('flowguard_refresh_token', data.refreshToken);
    localStorage.setItem('flowguard_user', JSON.stringify(data.user));
    setToken(data.accessToken);
    setUser(data.user);
    return data;
  }, []);

  const register = useCallback(async (payload: RegisterRequest): Promise<AuthResponse> => {
    const data = await authService.register(payload);
    localStorage.setItem('flowguard_access_token', data.accessToken);
    localStorage.setItem('flowguard_refresh_token', data.refreshToken);
    localStorage.setItem('flowguard_user', JSON.stringify(data.user));
    setToken(data.accessToken);
    setUser(data.user);
    return data;
  }, []);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } finally {
      setUser(null);
      setToken(null);
    }
  }, []);

  const hasRole = useCallback(
    (rolesToCheck: RoleType | RoleType[]): boolean => {
      if (!user || !user.roles) return false;
      const checkArray = Array.isArray(rolesToCheck) ? rolesToCheck : [rolesToCheck];
      return checkArray.some((r) => user.roles.includes(r));
    },
    [user]
  );

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!user && !!token,
        isLoading,
        login,
        register,
        logout,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

