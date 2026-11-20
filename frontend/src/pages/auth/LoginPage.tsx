import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ShieldAlert, Lock, Mail, ArrowRight, Zap, Eye, EyeOff } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';

export const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();
  const { success, error } = useToast();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const cleanEmail = email.trim();
    if (!cleanEmail || !password) {
      error('Validation Error', 'Please enter both email and password.');
      return;
    }

    setIsLoading(true);
    try {
      await login({ email: cleanEmail, password });
      success('Authentication Successful', 'Welcome to FlowGuard AI Observability Platform.');
      navigate('/dashboard');
    } catch (err: any) {
      if (!err.response) {
        error(
          'Backend Unavailable',
          'Cannot reach the FlowGuard backend on port 8080. Please ensure the backend server is running.'
        );
      } else {
        const msg = err.response?.data?.message || 'Invalid email or password. Please verify credentials.';
        error('Authentication Failed', msg);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleDemoFill = (demoEmail: string, demoPass: string) => {
    setEmail(demoEmail);
    setPassword(demoPass);
  };

  return (
    <div className="min-h-screen bg-navy-950 flex flex-col justify-center py-12 sm:px-6 lg:px-8 relative overflow-hidden">
      {/* Background radial glow */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-electric-600/10 rounded-full blur-3xl pointer-events-none" />

      <div className="sm:mx-auto sm:w-full sm:max-w-md relative z-10">
        <div className="flex justify-center">
          <div className="w-12 h-12 rounded-xl bg-electric-600/20 border border-electric-500/40 flex items-center justify-center text-electric-400 shadow-xl shadow-electric-600/20">
            <ShieldAlert className="w-7 h-7 text-electric-400" />
          </div>
        </div>
        <h2 className="mt-4 text-center text-2xl font-bold tracking-tight text-white font-sans">
          FlowGuard <span className="text-electric-400 font-mono text-sm px-1.5 py-0.5 rounded bg-electric-500/20">AI</span>
        </h2>
        <p className="mt-1 text-center text-xs text-slate-400">
          Intelligent API Monitoring &amp; Transaction Intelligence
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md relative z-10">
        <div className="bg-navy-900 border border-navy-750 py-8 px-6 shadow-2xl rounded-2xl sm:px-10">
          <form className="space-y-4" onSubmit={handleSubmit}>
            <Input
              label="Work Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="admin@flowguard.ai"
              leftIcon={<Mail className="w-4 h-4" />}
              required
            />

            <Input
              label="Password"
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••••••"
              leftIcon={<Lock className="w-4 h-4" />}
              rightIcon={
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="text-slate-400 hover:text-slate-200 transition-colors focus:outline-none"
                  tabIndex={-1}
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              }
              required
            />

            <div className="flex items-center justify-between text-xs">
              <label className="flex items-center text-slate-400">
                <input
                  type="checkbox"
                  className="rounded border-navy-700 bg-navy-950 text-electric-600 focus:ring-electric-500"
                  defaultChecked
                />
                <span className="ml-2">Remember session</span>
              </label>

              <Link
                to="/forgot-password"
                className="font-medium text-electric-400 hover:text-electric-300 transition-colors"
              >
                Forgot password?
              </Link>
            </div>

            <Button
              type="submit"
              variant="primary"
              size="md"
              className="w-full mt-2"
              isLoading={isLoading}
              rightIcon={<ArrowRight className="w-4 h-4" />}
            >
              Sign In to FlowGuard
            </Button>
          </form>

          {/* Quick Demo Credentials */}
          <div className="mt-6 pt-5 border-t border-navy-750/80">
            <p className="text-[11px] font-semibold uppercase tracking-wider text-slate-400 mb-2 flex items-center gap-1.5">
              <Zap className="w-3.5 h-3.5 text-amber-400" />
              1-Click Demo Accounts (Click to Fill):
            </p>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => handleDemoFill('admin@flowguard.ai', 'Admin@123456')}
                className="px-2 py-2 text-[11px] font-medium rounded-lg bg-navy-950/80 border border-navy-750 text-slate-300 hover:border-electric-500 hover:text-white hover:bg-electric-500/10 transition-all text-center flex flex-col items-center gap-0.5"
              >
                <span className="font-semibold text-electric-400">Super Admin</span>
                <span className="text-[10px] text-slate-400 font-mono">Admin@123456</span>
              </button>
              <button
                type="button"
                onClick={() => handleDemoFill('engineer@flowguard.ai', 'Engineer@123456')}
                className="px-2 py-2 text-[11px] font-medium rounded-lg bg-navy-950/80 border border-navy-750 text-slate-300 hover:border-electric-500 hover:text-white hover:bg-electric-500/10 transition-all text-center flex flex-col items-center gap-0.5"
              >
                <span className="font-semibold text-emerald-400">Engineer</span>
                <span className="text-[10px] text-slate-400 font-mono">Engineer@123456</span>
              </button>
              <button
                type="button"
                onClick={() => handleDemoFill('viewer@flowguard.ai', 'Viewer@123456')}
                className="px-2 py-2 text-[11px] font-medium rounded-lg bg-navy-950/80 border border-navy-750 text-slate-300 hover:border-electric-500 hover:text-white hover:bg-electric-500/10 transition-all text-center flex flex-col items-center gap-0.5"
              >
                <span className="font-semibold text-amber-400">Viewer</span>
                <span className="text-[10px] text-slate-400 font-mono">Viewer@123456</span>
              </button>
            </div>
            <p className="mt-2 text-[10px] text-slate-500 text-center">
              Click any role above to automatically fill credentials into form
            </p>
          </div>

          <div className="mt-6 text-center text-xs text-slate-400">
            Need an enterprise account?{' '}
            <Link to="/register" className="font-semibold text-electric-400 hover:text-electric-300">
              Register here
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

