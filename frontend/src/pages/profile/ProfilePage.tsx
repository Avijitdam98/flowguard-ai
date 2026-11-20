import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { User, Lock, Phone, Mail, Shield, Save } from 'lucide-react';
import api from '../../services/api';

export const ProfilePage: React.FC = () => {
  const { user } = useAuth();
  const { success, error } = useToast();

  const [profileData, setProfileData] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    phone: user?.phone || '',
  });

  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const [isUpdatingProfile, setIsUpdatingProfile] = useState(false);
  const [isChangingPassword, setIsChangingPassword] = useState(false);

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsUpdatingProfile(true);
    try {
      await api.put('/users/profile', profileData);
      success('Profile Updated', 'Your profile details have been saved.');
    } catch (err: any) {
      error('Update Failed', err.response?.data?.message || 'Failed to update profile.');
    } finally {
      setIsUpdatingProfile(false);
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      error('Validation Error', 'New passwords do not match.');
      return;
    }

    setIsChangingPassword(true);
    try {
      await api.put('/users/password', passwordData);
      success('Password Changed', 'Your security password has been updated.');
      setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err: any) {
      error('Change Password Failed', err.response?.data?.message || 'Current password invalid.');
    } finally {
      setIsChangingPassword(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-xl font-bold tracking-tight text-white">Account Profile &amp; Credentials</h1>
        <p className="text-xs text-slate-400 mt-0.5">
          Manage your personal details, workspace security, and authentication credentials
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* User Identity Card */}
        <Card className="md:col-span-1">
          <div className="flex flex-col items-center text-center p-4">
            <div className="w-20 h-20 rounded-full bg-electric-600/20 border-2 border-electric-500/50 flex items-center justify-center text-electric-400 text-2xl font-bold mb-3 shadow-lg shadow-electric-600/20">
              {user?.firstName?.[0] || 'U'}
            </div>
            <h3 className="text-base font-bold text-white">{user?.firstName} {user?.lastName}</h3>
            <p className="text-xs text-slate-400 font-mono mt-0.5">{user?.email}</p>

            <div className="mt-4 flex flex-wrap gap-1.5 justify-center">
              {user?.roles?.map((role) => (
                <StatusBadge key={role} status={role} size="sm" />
              ))}
            </div>

            <div className="w-full border-t border-navy-750/70 mt-6 pt-4 text-left text-xs space-y-2">
              <div className="flex justify-between">
                <span className="text-slate-400">Account ID:</span>
                <span className="font-mono text-slate-300 truncate max-w-[140px]">{user?.id}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Status:</span>
                <span className="text-emerald-400 font-semibold">Active</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Email Verified:</span>
                <span className="text-emerald-400 font-semibold">Verified</span>
              </div>
            </div>
          </div>
        </Card>

        {/* Edit Profile & Password Forms */}
        <div className="md:col-span-2 space-y-6">
          <Card title="Personal Information" subtitle="Update your account name and contact number">
            <form onSubmit={handleUpdateProfile} className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <Input
                  label="First Name"
                  value={profileData.firstName}
                  onChange={(e) => setProfileData({ ...profileData, firstName: e.target.value })}
                  leftIcon={<User className="w-4 h-4" />}
                  required
                />
                <Input
                  label="Last Name"
                  value={profileData.lastName}
                  onChange={(e) => setProfileData({ ...profileData, lastName: e.target.value })}
                  required
                />
              </div>

              <Input
                label="Email Address"
                value={user?.email || ''}
                disabled
                helperText="Email address is governed by platform SSO and administrator policy."
                leftIcon={<Mail className="w-4 h-4" />}
              />

              <Input
                label="Phone Number"
                value={profileData.phone}
                onChange={(e) => setProfileData({ ...profileData, phone: e.target.value })}
                placeholder="+1-555-0100"
                leftIcon={<Phone className="w-4 h-4" />}
              />

              <div className="flex justify-end pt-2">
                <Button
                  type="submit"
                  variant="primary"
                  size="sm"
                  isLoading={isUpdatingProfile}
                  leftIcon={<Save className="w-3.5 h-3.5" />}
                >
                  Save Profile Changes
                </Button>
              </div>
            </form>
          </Card>

          <Card title="Security Credentials" subtitle="Ensure your account is protected with a strong password">
            <form onSubmit={handleChangePassword} className="space-y-4">
              <Input
                label="Current Password"
                type="password"
                value={passwordData.currentPassword}
                onChange={(e) => setPasswordData({ ...passwordData, currentPassword: e.target.value })}
                leftIcon={<Lock className="w-4 h-4" />}
                required
              />

              <Input
                label="New Password"
                type="password"
                value={passwordData.newPassword}
                onChange={(e) => setPasswordData({ ...passwordData, newPassword: e.target.value })}
                helperText="Password must be at least 8 characters long."
                leftIcon={<Lock className="w-4 h-4" />}
                required
              />

              <Input
                label="Confirm New Password"
                type="password"
                value={passwordData.confirmPassword}
                onChange={(e) => setPasswordData({ ...passwordData, confirmPassword: e.target.value })}
                leftIcon={<Lock className="w-4 h-4" />}
                required
              />

              <div className="flex justify-end pt-2">
                <Button
                  type="submit"
                  variant="secondary"
                  size="sm"
                  isLoading={isChangingPassword}
                  leftIcon={<Shield className="w-3.5 h-3.5" />}
                >
                  Update Account Password
                </Button>
              </div>
            </form>
          </Card>
        </div>
      </div>
    </div>
  );
};

