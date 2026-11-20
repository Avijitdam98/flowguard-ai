import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { ShieldAlert, Mail, ArrowLeft, Send, CheckCircle2 } from 'lucide-react';
import { authService } from '../../services/authService';
import { useToast } from '../../context/ToastContext';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';

export const ForgotPasswordPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitted, setIsSubmitted] = useState(false);
  const { error } = useToast();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) {
      error('Validation Error', 'Please enter your work email.');
      return;
    }

    setIsLoading(true);
    try {
      await authService.forgotPassword({ email });
      setIsSubmitted(true);
    } catch {
      // Show submitted anyway for user privacy
      setIsSubmitted(true);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-navy-950 flex flex-col justify-center py-12 sm:px-6 lg:px-8 relative overflow-hidden">
      <div className="sm:mx-auto sm:w-full sm:max-w-md relative z-10">
        <div className="flex justify-center">
          <div className="w-12 h-12 rounded-xl bg-electric-600/20 border border-electric-500/40 flex items-center justify-center text-electric-400">
            <ShieldAlert className="w-7 h-7 text-electric-400" />
          </div>
        </div>
        <h2 className="mt-4 text-center text-2xl font-bold tracking-tight text-white font-sans">
          Reset Password
        </h2>
        <p className="mt-1 text-center text-xs text-slate-400">
          Enter your registered email to receive password recovery instructions
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md relative z-10">
        <div className="bg-navy-900 border border-navy-750 py-8 px-6 shadow-2xl rounded-2xl sm:px-10">
          {isSubmitted ? (
            <div className="text-center space-y-4">
              <div className="w-12 h-12 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 flex items-center justify-center mx-auto">
                <CheckCircle2 className="w-6 h-6" />
              </div>
              <h3 className="text-sm font-semibold text-white">Recovery Link Sent</h3>
              <p className="text-xs text-slate-400 leading-relaxed">
                If an account exists with <span className="text-slate-200 font-mono">{email}</span>, we have dispatched a secure password reset link. Please check your inbox and spam folder.
              </p>
              <Link to="/login" className="inline-block mt-4">
                <Button variant="secondary" size="sm" leftIcon={<ArrowLeft className="w-4 h-4" />}>
                  Return to Sign In
                </Button>
              </Link>
            </div>
          ) : (
            <form className="space-y-4" onSubmit={handleSubmit}>
              <Input
                label="Work Email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="engineer@flowguard.ai"
                leftIcon={<Mail className="w-4 h-4" />}
                required
              />

              <Button
                type="submit"
                variant="primary"
                size="md"
                className="w-full mt-2"
                isLoading={isLoading}
                rightIcon={<Send className="w-4 h-4" />}
              >
                Send Recovery Instructions
              </Button>

              <div className="text-center mt-4">
                <Link
                  to="/login"
                  className="text-xs text-slate-400 hover:text-white flex items-center justify-center gap-1.5 transition-colors"
                >
                  <ArrowLeft className="w-3.5 h-3.5" /> Back to Sign In
                </Link>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

