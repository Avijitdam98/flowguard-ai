import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import { ToastProvider } from './context/ToastContext';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { AppLayout } from './components/layout/AppLayout';

// Auth Pages
import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';
import { ForgotPasswordPage } from './pages/auth/ForgotPasswordPage';

// Application Pages
import { DashboardPage } from './pages/dashboard/DashboardPage';
import { MonitorsPage } from './pages/monitors/MonitorsPage';
import { MonitorDetailPage } from './pages/monitors/MonitorDetailPage';
import { TransactionsPage } from './pages/transactions/TransactionsPage';
import { TransactionDetailPage } from './pages/transactions/TransactionDetailPage';
import { ServicesPage } from './pages/services/ServicesPage';
import { ServiceDetailPage } from './pages/services/ServiceDetailPage';
import { LogsPage } from './pages/logs/LogsPage';
import { IncidentsPage } from './pages/incidents/IncidentsPage';
import { IncidentDetailPage } from './pages/incidents/IncidentDetailPage';
import { AlertsPage } from './pages/alerts/AlertsPage';
import { AnalyticsPage } from './pages/analytics/AnalyticsPage';
import { AiAssistantPage } from './pages/ai/AiAssistantPage';
import { SettingsPage } from './pages/settings/SettingsPage';
import { ProfilePage } from './pages/profile/ProfilePage';

export default function App() {
  return (
    <ThemeProvider>
      <ToastProvider>
        <AuthProvider>
          <BrowserRouter>
            <Routes>
              {/* Public Authentication Routes */}
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="/forgot-password" element={<ForgotPasswordPage />} />

              {/* Protected SaaS Application Routes */}
              <Route element={<ProtectedRoute />}>
                <Route element={<AppLayout />}>
                  <Route path="/" element={<Navigate to="/dashboard" replace />} />
                  <Route path="/dashboard" element={<DashboardPage />} />
                  <Route path="/api-monitors" element={<MonitorsPage />} />
                  <Route path="/api-monitors/:id" element={<MonitorDetailPage />} />
                  <Route path="/transactions" element={<TransactionsPage />} />
                  <Route path="/transactions/:id" element={<TransactionDetailPage />} />
                  <Route path="/services" element={<ServicesPage />} />
                  <Route path="/services/:id" element={<ServiceDetailPage />} />
                  <Route path="/logs" element={<LogsPage />} />
                  <Route path="/incidents" element={<IncidentsPage />} />
                  <Route path="/incidents/:id" element={<IncidentDetailPage />} />
                  <Route path="/alerts" element={<AlertsPage />} />
                  <Route path="/analytics" element={<AnalyticsPage />} />
                  <Route path="/ai-assistant" element={<AiAssistantPage />} />
                  <Route path="/settings" element={<SettingsPage />} />
                  <Route path="/profile" element={<ProfilePage />} />
                </Route>
              </Route>

              {/* Fallback */}
              <Route path="*" element={<Navigate to="/dashboard" replace />} />
            </Routes>
          </BrowserRouter>
        </AuthProvider>
      </ToastProvider>
    </ThemeProvider>
  );
}
