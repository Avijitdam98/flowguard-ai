import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ShieldAlert, Lock, Mail, User, Phone, ArrowRight } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';

export const RegisterPage: React.FC = () => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
  });
  const [isLoading, setIsLoading] = useState(false);
  const { register } = useAuth();
  const { success, error } = useToast();
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (formData.password !== formData.confirmPassword) {
      error('Validation Error', 'Passwords do not match.');
      return;
    }

    if (formData.password.length < 8) {
      error('Validation Error', 'Password must be at least 8 characters long.');
      return;
    }

    setIsLoading(true);
    try {
      await register({
        firstName: formData.firstName,
        lastName: formData.lastName,
        email: formData.email,
        phone: formData.phone || undefined,
        password: formData.password,
      });
      success('Account Created', 'Welcome to FlowGuard AI. Your account has been provisioned.');
      navigate('/dashboard');
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Registration failed. Please check your details.';
      error('Registration Error', msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-navy-950 flex flex-col justify-center py-12 sm:px-6 lg:px-8 relative overflow-hidden">
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-electric-600/10 rounded-full blur-3xl pointer-events-none" />

      <div className="sm:mx-auto sm:w-full sm:max-w-md relative z-10">
        <div className="flex justify-center">
          <div className="w-12 h-12 rounded-xl bg-electric-600/20 border border-electric-500/40 flex items-center justify-center text-electric-400">
            <ShieldAlert className="w-7 h-7 text-electric-400" />
          </div>
        </div>
        <h2 className="mt-4 text-center text-2xl font-bold tracking-tight text-white font-sans">
          Create FlowGuard AI Account
        </h2>
        <p className="mt-1 text-center text-xs text-slate-400">
          Set up your engineering observability workspace
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md relative z-10">
        <div className="bg-navy-900 border border-navy-750 py-8 px-6 shadow-2xl rounded-2xl sm:px-10">
          <form className="space-y-4" onSubmit={handleSubmit}>
            <div className="grid grid-cols-2 gap-3">
              <Input
                label="First Name"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                placeholder="Jane"
                leftIcon={<User className="w-4 h-4" />}
                required
              />
              <Input
                label="Last Name"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                placeholder="Doe"
                required
              />
            </div>

            <Input
              label="Work Email"
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="jane@company.com"
              leftIcon={<Mail className="w-4 h-4" />}
              required
            />

            <Input
              label="Phone Number (Optional)"
              type="tel"
              name="phone"
              value={formData.phone}
              onChange={handleChange}
              placeholder="+1-555-0199"
              leftIcon={<Phone className="w-4 h-4" />}
            />

            <Input
              label="Password (min 8 characters)"
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="••••••••••••"
              leftIcon={<Lock className="w-4 h-4" />}
              required
            />

            <Input
              label="Confirm Password"
              type="password"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              placeholder="••••••••••••"
              leftIcon={<Lock className="w-4 h-4" />}
              required
            />

            <Button
              type="submit"
              variant="primary"
              size="md"
              className="w-full mt-2"
              isLoading={isLoading}
              rightIcon={<ArrowRight className="w-4 h-4" />}
            >
              Provision Account
            </Button>
          </form>

          <div className="mt-6 text-center text-xs text-slate-400">
            Already have an account?{' '}
            <Link to="/login" className="font-semibold text-electric-400 hover:text-electric-300">
              Sign In
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

