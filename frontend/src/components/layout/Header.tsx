import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Search,
  Bell,
  Sun,
  Moon,
  LogOut,
  User,
  Shield,
  CheckCircle,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import { useToast } from '../../context/ToastContext';

export const Header: React.FC = () => {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const { info } = useToast();
  const navigate = useNavigate();
  const [userMenuOpen, setUserMenuOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <header className="h-16 border-b border-navy-750 bg-navy-900/80 backdrop-blur-md px-6 flex items-center justify-between z-20">
      {/* Search Input */}
      <div className="flex items-center gap-4 flex-1 max-w-md">
        <div className="relative w-full">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search APIs, transactions, trace IDs (Press '/' to focus)..."
            className="w-full bg-navy-950/80 border border-navy-750 rounded-lg pl-9 pr-3.5 py-1.5 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-electric-500"
          />
        </div>
      </div>

      {/* Right Controls */}
      <div className="flex items-center gap-3">
        {/* Environment Indicator */}
        <div className="hidden sm:inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-mono bg-emerald-500/10 border border-emerald-500/30 text-emerald-400">
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
          PRODUCTION
        </div>

        {/* Theme Toggle */}
        <button
          onClick={toggleTheme}
          className="p-2 rounded-lg text-slate-400 hover:text-white hover:bg-navy-800 transition-colors"
          title={`Switch to ${theme === 'dark' ? 'light' : 'dark'} mode`}
        >
          {theme === 'dark' ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
        </button>

        {/* Notification Bell */}
        <button
          onClick={() => info('Notifications', '1 critical incident active in Payment Service')}
          className="relative p-2 rounded-lg text-slate-400 hover:text-white hover:bg-navy-800 transition-colors"
        >
          <Bell className="w-4 h-4" />
          <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-rose-500" />
        </button>

        <div className="h-6 w-px bg-navy-750 mx-1" />

        {/* User Menu Dropdown */}
        <div className="relative">
          <button
            onClick={() => setUserMenuOpen(!userMenuOpen)}
            className="flex items-center gap-2.5 p-1.5 rounded-lg hover:bg-navy-800 transition-colors"
          >
            <div className="w-7 h-7 rounded-full bg-electric-600/30 border border-electric-500/50 flex items-center justify-center text-electric-300 font-semibold text-xs">
              {user?.firstName?.[0] || 'U'}
            </div>
            <div className="hidden md:flex flex-col text-left">
              <span className="text-xs font-semibold text-slate-200 leading-tight">
                {user?.firstName} {user?.lastName}
              </span>
              <span className="text-[10px] text-slate-400 leading-tight">
                {user?.roles?.[0] || 'ENGINEER'}
              </span>
            </div>
          </button>

          {userMenuOpen && (
            <div
              className="absolute right-0 mt-2 w-56 bg-navy-900 border border-navy-750 rounded-xl shadow-2xl py-2 z-50 animate-fade-in"
              onClick={() => setUserMenuOpen(false)}
            >
              <div className="px-4 py-2 border-b border-navy-750">
                <p className="text-xs font-semibold text-slate-200">{user?.firstName} {user?.lastName}</p>
                <p className="text-[10px] text-slate-400 truncate">{user?.email}</p>
                <div className="mt-1.5 flex items-center gap-1 text-[10px] text-electric-400">
                  <Shield className="w-3 h-3" />
                  <span>Role: {user?.roles?.join(', ')}</span>
                </div>
              </div>

              <button
                onClick={() => navigate('/profile')}
                className="w-full px-4 py-2 text-left text-xs text-slate-300 hover:bg-navy-800 flex items-center gap-2"
              >
                <User className="w-3.5 h-3.5 text-slate-400" />
                Profile &amp; Credentials
              </button>

              <button
                onClick={() => navigate('/settings')}
                className="w-full px-4 py-2 text-left text-xs text-slate-300 hover:bg-navy-800 flex items-center gap-2"
              >
                <CheckCircle className="w-3.5 h-3.5 text-slate-400" />
                Workspace Settings
              </button>

              <div className="border-t border-navy-750 my-1" />

              <button
                onClick={handleLogout}
                className="w-full px-4 py-2 text-left text-xs text-rose-400 hover:bg-rose-950/30 flex items-center gap-2"
              >
                <LogOut className="w-3.5 h-3.5 text-rose-400" />
                Sign Out
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

