import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { RoleType } from '../../types/auth';
import { Loader2 } from 'lucide-react';

interface ProtectedRouteProps {
  allowedRoles?: RoleType[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ allowedRoles }) => {
  const { isAuthenticated, isLoading, user } = useAuth();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-navy-950 text-slate-100">
        <div className="flex flex-col items-center gap-3">
          <Loader2 className="w-8 h-8 animate-spin text-electric-500" />
          <p className="text-xs text-slate-400 font-mono tracking-wider">INITIALIZING SESSION...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && user && !allowedRoles.some((r) => user.roles.includes(r))) {
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
};

