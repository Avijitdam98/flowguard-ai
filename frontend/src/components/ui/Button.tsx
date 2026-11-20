import React, { ButtonHTMLAttributes, ReactNode } from 'react';
import { Loader2 } from 'lucide-react';

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost' | 'outline';
  size?: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
  leftIcon?: ReactNode;
  rightIcon?: ReactNode;
  children: ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  isLoading = false,
  leftIcon,
  rightIcon,
  children,
  className = '',
  disabled,
  ...props
}) => {
  const baseStyles = 'inline-flex items-center justify-center font-medium rounded-lg transition-all focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed';

  const sizeStyles = {
    sm: 'text-xs px-2.5 py-1.5 gap-1.5',
    md: 'text-sm px-4 py-2 gap-2',
    lg: 'text-base px-5 py-2.5 gap-2.5',
  };

  const variantStyles = {
    primary: 'bg-electric-600 hover:bg-electric-500 text-white shadow-lg shadow-electric-600/25 focus:ring-electric-500 focus:ring-offset-navy-900 border border-electric-500/30',
    secondary: 'bg-navy-800 hover:bg-navy-750 text-slate-200 border border-navy-700 hover:border-slate-500 focus:ring-slate-500 focus:ring-offset-navy-900',
    danger: 'bg-rose-600 hover:bg-rose-500 text-white shadow-lg shadow-rose-600/25 focus:ring-rose-500 focus:ring-offset-navy-900 border border-rose-500/30',
    ghost: 'bg-transparent hover:bg-navy-800 text-slate-300 hover:text-white focus:ring-slate-500 focus:ring-offset-navy-900',
    outline: 'bg-transparent border border-electric-500 text-electric-400 hover:bg-electric-500/10 focus:ring-electric-500 focus:ring-offset-navy-900',
  };

  return (
    <button
      className={`${baseStyles} ${sizeStyles[size]} ${variantStyles[variant]} ${className}`}
      disabled={disabled || isLoading}
      {...props}
    >
      {isLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : leftIcon}
      {children}
      {!isLoading && rightIcon}
    </button>
  );
};

