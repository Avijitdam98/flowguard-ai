import React from 'react';

export interface LoadingSkeletonProps {
  className?: string;
  count?: number;
  height?: string;
}

export const LoadingSkeleton: React.FC<LoadingSkeletonProps> = ({
  className = '',
  count = 1,
  height = 'h-4',
}) => {
  return (
    <div className="space-y-2.5 w-full animate-pulse">
      {Array.from({ length: count }).map((_, i) => (
        <div
          key={i}
          className={`bg-navy-800/80 border border-navy-700/50 rounded-lg ${height} ${className}`}
        />
      ))}
    </div>
  );
};

