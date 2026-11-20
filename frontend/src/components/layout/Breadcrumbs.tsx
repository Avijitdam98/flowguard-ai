import React from 'react';
import { useLocation, Link } from 'react-router-dom';
import { ChevronRight, Home } from 'lucide-react';

export const Breadcrumbs: React.FC = () => {
  const location = useLocation();
  const pathnames = location.pathname.split('/').filter((x) => x);

  const formatName = (str: string) => {
    return str
      .replace(/-/g, ' ')
      .replace(/\b\w/g, (char) => char.toUpperCase());
  };

  return (
    <nav className="flex items-center text-xs text-slate-400 py-3 px-6 bg-navy-950/40 border-b border-navy-750/50">
      <Link
        to="/dashboard"
        className="flex items-center gap-1 hover:text-slate-200 transition-colors"
      >
        <Home className="w-3.5 h-3.5" />
        <span>FlowGuard</span>
      </Link>

      {pathnames.map((value, index) => {
        const to = `/${pathnames.slice(0, index + 1).join('/')}`;
        const isLast = index === pathnames.length - 1;

        return (
          <React.Fragment key={to}>
            <ChevronRight className="w-3.5 h-3.5 mx-1.5 text-slate-600 flex-shrink-0" />
            {isLast ? (
              <span className="font-medium text-slate-200">{formatName(value)}</span>
            ) : (
              <Link to={to} className="hover:text-slate-200 transition-colors">
                {formatName(value)}
              </Link>
            )}
          </React.Fragment>
        );
      })}
    </nav>
  );
};

