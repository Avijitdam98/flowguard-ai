import React from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Header } from './Header';
import { Breadcrumbs } from './Breadcrumbs';

export const AppLayout: React.FC = () => {
  return (
    <div className="flex h-screen bg-navy-950 text-slate-100 overflow-hidden font-sans">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <Header />
        <Breadcrumbs />
        <main className="flex-1 overflow-y-auto p-6 bg-navy-950">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

