import React, { ReactNode } from 'react';
import { Inbox } from 'lucide-react';

export interface EmptyStateProps {
  icon?: ReactNode;
  title: string;
  description: string;
  action?: ReactNode;
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  icon,
  title,
  description,
  action,
}) => {
  return (
    <div className="flex flex-col items-center justify-center p-8 text-center rounded-xl border border-dashed border-navy-700 bg-navy-900/40">
      <div className="w-12 h-12 rounded-full bg-navy-800 flex items-center justify-center text-slate-400 mb-3 border border-navy-700">
        {icon || <Inbox className="w-6 h-6 text-slate-400" />}
      </div>
      <h4 className="text-base font-semibold text-slate-200">{title}</h4>
      <p className="text-xs text-slate-400 max-w-sm mt-1 mb-4">{description}</p>
      {action && <div>{action}</div>}
    </div>
  );
};

