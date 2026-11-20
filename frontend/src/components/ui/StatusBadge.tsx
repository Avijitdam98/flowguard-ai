import React from 'react';

export interface StatusBadgeProps {
  status: string;
  size?: 'sm' | 'md';
  pulse?: boolean;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status, size = 'md', pulse = false }) => {
  const normalized = status.toUpperCase();

  const getStyle = (s: string) => {
    switch (s) {
      case 'HEALTHY':
      case 'SUCCESS':
      case 'RESOLVED':
      case 'CLOSED':
      case 'UP':
        return {
          bg: 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400',
          dot: 'bg-emerald-400',
        };
      case 'DEGRADED':
      case 'MEDIUM':
      case 'WARN':
      case 'ACKNOWLEDGED':
      case 'PROCESSING':
        return {
          bg: 'bg-amber-500/10 border-amber-500/30 text-amber-400',
          dot: 'bg-amber-400',
        };
      case 'CRITICAL':
      case 'FAILED':
      case 'DOWN':
      case 'UNHEALTHY':
      case 'HIGH':
      case 'ERROR':
      case 'FATAL':
        return {
          bg: 'bg-rose-500/10 border-rose-500/30 text-rose-400',
          dot: 'bg-rose-400',
        };
      case 'DETECTED':
      case 'INVESTIGATING':
      case 'PENDING':
      case 'INFO':
      case 'LOW':
      default:
        return {
          bg: 'bg-electric-500/10 border-electric-500/30 text-electric-400',
          dot: 'bg-electric-400',
        };
    }
  };

  const style = getStyle(normalized);

  const sizeClasses = size === 'sm' ? 'px-2 py-0.5 text-xs' : 'px-2.5 py-1 text-xs';

  return (
    <span
      className={`inline-flex items-center gap-1.5 font-medium rounded-full border ${style.bg} ${sizeClasses}`}
    >
      <span
        className={`w-1.5 h-1.5 rounded-full ${style.dot} ${pulse ? 'animate-pulse' : ''}`}
      />
      {normalized}
    </span>
  );
};

