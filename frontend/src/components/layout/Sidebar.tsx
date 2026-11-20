import React, { useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  Activity,
  Layers,
  Network,
  Terminal,
  AlertOctagon,
  BellRing,
  BarChart3,
  Bot,
  Settings,
  ChevronLeft,
  ChevronRight,
  ShieldAlert,
  UserCheck,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export const Sidebar: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const location = useLocation();
  const { user } = useAuth();

  const navItems = [
    { label: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { label: 'API Monitors', path: '/api-monitors', icon: Activity },
    { label: 'Transactions', path: '/transactions', icon: Layers },
    { label: 'Services', path: '/services', icon: Network },
    { label: 'Logs', path: '/logs', icon: Terminal },
    { label: 'Incidents', path: '/incidents', icon: AlertOctagon, badge: '1' },
    { label: 'Alerts', path: '/alerts', icon: BellRing },
    { label: 'Analytics', path: '/analytics', icon: BarChart3 },
    { label: 'AI Assistant', path: '/ai-assistant', icon: Bot, isAi: true },
    { label: 'Settings', path: '/settings', icon: Settings },
  ];

  return (
    <aside
      className={`relative flex flex-col border-r border-navy-750 bg-navy-900 transition-all duration-300 z-30 ${
        collapsed ? 'w-20' : 'w-64'
      }`}
    >
      {/* Brand Header */}
      <div className="flex items-center justify-between h-16 px-4 border-b border-navy-750">
        <div className="flex items-center gap-3 overflow-hidden">
          <div className="w-9 h-9 rounded-lg bg-electric-600/20 border border-electric-500/40 flex items-center justify-center text-electric-400 flex-shrink-0">
            <ShieldAlert className="w-5 h-5 text-electric-400" />
          </div>
          {!collapsed && (
            <div className="flex flex-col">
              <span className="text-sm font-bold tracking-tight text-white flex items-center gap-1.5">
                FlowGuard <span className="text-xs px-1 py-0.2 rounded bg-electric-500/20 text-electric-400 font-mono">AI</span>
              </span>
              <span className="text-[10px] text-slate-400 uppercase tracking-widest font-semibold">Observability</span>
            </div>
          )}
        </div>
        <button
          onClick={() => setCollapsed(!collapsed)}
          className="p-1 rounded-md text-slate-400 hover:text-white hover:bg-navy-800 transition-colors"
        >
          {collapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
        </button>
      </div>

      {/* Navigation Links */}
      <div className="flex-1 py-4 px-3 space-y-1 overflow-y-auto">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = location.pathname.startsWith(item.path);

          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-xs font-medium transition-all group ${
                isActive
                  ? 'bg-electric-600/15 text-electric-400 border border-electric-500/30'
                  : 'text-slate-400 hover:text-slate-100 hover:bg-navy-800/60'
              }`}
              title={collapsed ? item.label : undefined}
            >
              <Icon
                className={`w-4 h-4 flex-shrink-0 transition-colors ${
                  isActive ? 'text-electric-400' : 'text-slate-400 group-hover:text-slate-200'
                } ${item.isAi ? 'text-purple-400' : ''}`}
              />
              {!collapsed && (
                <span className="flex-1 truncate">{item.label}</span>
              )}
              {!collapsed && item.badge && (
                <span className="px-1.5 py-0.5 text-[10px] font-bold rounded-full bg-rose-500/20 text-rose-400 border border-rose-500/30">
                  {item.badge}
                </span>
              )}
            </NavLink>
          );
        })}
      </div>

      {/* User Status Footer */}
      {!collapsed && user && (
        <div className="p-3 border-t border-navy-750 bg-navy-950/40">
          <NavLink
            to="/profile"
            className="flex items-center gap-3 p-2 rounded-lg hover:bg-navy-800/60 transition-colors"
          >
            <div className="w-8 h-8 rounded-full bg-navy-800 border border-navy-700 flex items-center justify-center text-electric-400">
              <UserCheck className="w-4 h-4" />
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-xs font-semibold text-slate-200 truncate">{user.firstName} {user.lastName}</p>
              <p className="text-[10px] text-slate-400 truncate">{user.email}</p>
            </div>
          </NavLink>
        </div>
      )}
    </aside>
  );
};

