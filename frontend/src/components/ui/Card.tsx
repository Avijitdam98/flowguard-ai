import React, { ReactNode } from 'react';

export interface CardProps {
  title?: string;
  subtitle?: string;
  action?: ReactNode;
  children: ReactNode;
  className?: string;
  headerClassName?: string;
  bodyClassName?: string;
  footer?: ReactNode;
}

export const Card: React.FC<CardProps> = ({
  title,
  subtitle,
  action,
  children,
  className = '',
  headerClassName = '',
  bodyClassName = '',
  footer,
}) => {
  return (
    <div className={`bg-navy-900 border border-navy-750/80 rounded-xl shadow-lg transition-all ${className}`}>
      {(title || subtitle || action) && (
        <div className={`px-5 py-4 border-b border-navy-750/70 flex items-center justify-between gap-4 ${headerClassName}`}>
          <div>
            {title && <h3 className="text-base font-semibold text-slate-100 tracking-tight">{title}</h3>}
            {subtitle && <p className="text-xs text-slate-400 mt-0.5">{subtitle}</p>}
          </div>
          {action && <div className="flex-shrink-0">{action}</div>}
        </div>
      )}
      <div className={`p-5 ${bodyClassName}`}>{children}</div>
      {footer && <div className="px-5 py-3 border-t border-navy-750/70 bg-navy-950/40 rounded-b-xl">{footer}</div>}
    </div>
  );
};

