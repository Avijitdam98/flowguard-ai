import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import {
  Activity,
  Plus,
  Search,
  ExternalLink,
  Play,
  Trash2,
  RefreshCw,
  Clock,
  ShieldCheck,
  AlertTriangle,
  Radio,
} from 'lucide-react';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { LoadingSkeleton } from '../../components/ui/LoadingSkeleton';
import { EmptyState } from '../../components/ui/EmptyState';
import { Modal } from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import { monitorService } from '../../services/monitorService';
import {
  ApiMonitor,
  CreateApiMonitorRequest,
  EnvironmentType,
  HttpMethod,
  MonitorStatus,
} from '../../types/monitor';

export const MonitorsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const toast = useToast();

  const [searchTerm, setSearchTerm] = useState('');
  const [selectedEnv, setSelectedEnv] = useState<string>('ALL');
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [monitorToDelete, setMonitorToDelete] = useState<ApiMonitor | null>(null);

  // Form State for Create Monitor
  const [formData, setFormData] = useState<CreateApiMonitorRequest>({
    name: '',
    baseUrl: 'https://httpbin.org',
    endpointPath: '/status/200',
    httpMethod: 'GET',
    environment: 'PRODUCTION',
    monitoringIntervalSeconds: 60,
    timeoutMs: 5000,
    expectedHttpStatus: 200,
    alertThresholdMs: 2000,
    requestHeaders: '{\n  "Accept": "application/json"\n}',
    requestBody: '',
    isActive: true,
  });

  // Query monitors from backend
  const {
    data: paginatedData,
    isLoading,
    isFetching,
    refetch,
  } = useQuery({
    queryKey: ['monitors', selectedEnv, selectedStatus],
    queryFn: () =>
      monitorService.getAllMonitors({
        environment: selectedEnv !== 'ALL' ? (selectedEnv as EnvironmentType) : undefined,
        status: selectedStatus !== 'ALL' ? (selectedStatus as MonitorStatus) : undefined,
      }),
    refetchInterval: 30000,
  });

  // Create monitor mutation
  const createMutation = useMutation({
    mutationFn: (newMonitor: CreateApiMonitorRequest) => monitorService.createMonitor(newMonitor),
    onSuccess: (created) => {
      queryClient.invalidateQueries({ queryKey: ['monitors'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      toast.success('Monitor Created', `"${created.name}" registered with SSRF protection`);
      setIsCreateModalOpen(false);
      setFormData({
        name: '',
        baseUrl: 'https://httpbin.org',
        endpointPath: '/status/200',
        httpMethod: 'GET',
        environment: 'PRODUCTION',
        monitoringIntervalSeconds: 60,
        timeoutMs: 5000,
        expectedHttpStatus: 200,
        alertThresholdMs: 2000,
        requestHeaders: '{\n  "Accept": "application/json"\n}',
        requestBody: '',
        isActive: true,
      });
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to create monitor. Check SSRF rules or URL format.';
      toast.error('Registration Failed', msg);
    },
  });

  // Toggle active mutation
  const toggleMutation = useMutation({
    mutationFn: (id: string) => monitorService.toggleMonitor(id),
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ['monitors'] });
      toast.info('Monitor Updated', `${updated.name} probing is now ${updated.isActive ? 'Active' : 'Paused'}`);
    },
    onError: () => {
      toast.error('Error', 'Unable to toggle monitor state');
    },
  });

  // Run probe mutation
  const probeMutation = useMutation({
    mutationFn: (id: string) => monitorService.triggerProbe(id),
    onSuccess: (res, monitorId) => {
      queryClient.invalidateQueries({ queryKey: ['monitors'] });
      queryClient.invalidateQueries({ queryKey: ['monitor', monitorId] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      if (res.isSuccessful) {
        toast.success('Probe Successful', `HTTP ${res.statusCode} in ${res.responseTimeMs}ms`);
      } else {
        toast.warning('Probe Alert', `HTTP ${res.statusCode}: ${res.errorMessage || 'Failure detected'}`);
      }
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Probe request failed';
      toast.error('Probe Failed', msg);
    },
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: (id: string) => monitorService.deleteMonitor(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['monitors'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      toast.success('Monitor Deleted', 'Monitor was permanently removed');
      setMonitorToDelete(null);
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to delete monitor';
      toast.error('Delete Failed', msg);
    },
  });

  const handleCreateSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name.trim() || !formData.baseUrl.trim() || !formData.endpointPath.trim()) {
      toast.warning('Required Fields', 'Please complete Name, Base URL and Endpoint Path');
      return;
    }
    createMutation.mutate(formData);
  };

  const monitorsList = paginatedData?.content || [];
  const filteredMonitors = monitorsList.filter((m) =>
    m.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    m.endpointPath.toLowerCase().includes(searchTerm.toLowerCase()) ||
    m.baseUrl.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const getMethodBadgeClass = (method: string) => {
    switch (method.toUpperCase()) {
      case 'GET':
        return 'bg-electric-500/10 text-electric-400 border-electric-500/30';
      case 'POST':
        return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
      case 'PUT':
      case 'PATCH':
        return 'bg-amber-500/10 text-amber-400 border-amber-500/30';
      case 'DELETE':
        return 'bg-rose-500/10 text-rose-400 border-rose-500/30';
      default:
        return 'bg-slate-500/10 text-slate-400 border-slate-500/30';
    }
  };

  const getEnvBadge = (env: EnvironmentType) => {
    switch (env) {
      case 'PRODUCTION':
        return 'bg-rose-500/15 text-rose-300 border-rose-500/30';
      case 'STAGING':
        return 'bg-amber-500/15 text-amber-300 border-amber-500/30';
      case 'DEVELOPMENT':
        return 'bg-blue-500/15 text-blue-300 border-blue-500/30';
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
            <Activity className="w-5 h-5 text-electric-400" /> API Monitors
            {isFetching && <RefreshCw className="w-3.5 h-3.5 text-slate-400 animate-spin" />}
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Real-time SSRF-guarded health probes, uptime verification, and automated alerting thresholds
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => refetch()}
            leftIcon={<RefreshCw className={`w-3.5 h-3.5 ${isFetching ? 'animate-spin' : ''}`} />}
          >
            Refresh
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={() => setIsCreateModalOpen(true)}
            leftIcon={<Plus className="w-4 h-4" />}
          >
            Register New Monitor
          </Button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <Card className="p-3">
        <div className="flex flex-col md:flex-row gap-3 items-center justify-between">
          <div className="relative flex-1 w-full">
            <Search className="absolute left-3 top-2.5 w-4 h-4 text-slate-400" />
            <input
              type="text"
              placeholder="Search monitors by name, URL, or endpoint path..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full bg-navy-950/60 border border-navy-750 text-xs rounded-lg pl-9 pr-3 py-2 text-slate-200 placeholder-slate-500 focus:outline-none focus:border-electric-500"
            />
          </div>

          <div className="flex items-center gap-2 w-full md:w-auto">
            <select
              value={selectedEnv}
              onChange={(e) => setSelectedEnv(e.target.value)}
              className="bg-navy-950/60 border border-navy-750 text-xs rounded-lg px-2.5 py-2 text-slate-300 focus:outline-none focus:border-electric-500"
            >
              <option value="ALL">All Environments</option>
              <option value="PRODUCTION">Production</option>
              <option value="STAGING">Staging</option>
              <option value="DEVELOPMENT">Development</option>
            </select>

            <select
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
              className="bg-navy-950/60 border border-navy-750 text-xs rounded-lg px-2.5 py-2 text-slate-300 focus:outline-none focus:border-electric-500"
            >
              <option value="ALL">All Health States</option>
              <option value="HEALTHY">Healthy</option>
              <option value="DEGRADED">Degraded</option>
              <option value="DOWN">Down</option>
            </select>
          </div>
        </div>
      </Card>

      {/* Monitors Table */}
      <Card>
        {isLoading ? (
          <div className="p-4 space-y-3">
            <LoadingSkeleton count={8} height="h-10" />
          </div>
        ) : filteredMonitors.length === 0 ? (
          <EmptyState
            title="No Monitors Found"
            description={
              searchTerm || selectedEnv !== 'ALL' || selectedStatus !== 'ALL'
                ? 'Try adjusting your filters or search keywords.'
                : 'Start monitoring your microservices by registering your first HTTP endpoint.'
            }
            action={
              searchTerm ? undefined : (
                <Button variant="primary" size="sm" onClick={() => setIsCreateModalOpen(true)}>
                  Register Monitor
                </Button>
              )
            }
            icon={<Radio className="w-8 h-8 text-slate-500" />}
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-navy-750 text-[11px] font-semibold uppercase text-slate-400">
                  <th className="px-4 py-3">Monitor Name</th>
                  <th className="px-4 py-3">Method & Path</th>
                  <th className="px-4 py-3">Environment</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3">Interval / Timeout</th>
                  <th className="px-4 py-3">Probing</th>
                  <th className="px-4 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-navy-750/50 text-slate-200">
                {filteredMonitors.map((m) => (
                  <tr key={m.id} className="hover:bg-navy-800/40 transition-colors group">
                    <td className="px-4 py-3 font-medium text-white">
                      <Link
                        to={`/monitors/${m.id}`}
                        className="hover:text-electric-400 flex items-center gap-1.5 transition-colors"
                      >
                        {m.name}
                        <ExternalLink className="w-3 h-3 opacity-0 group-hover:opacity-100 transition-opacity text-slate-400" />
                      </Link>
                      <div className="text-[11px] text-slate-400 font-mono truncate max-w-xs">{m.baseUrl}</div>
                    </td>

                    <td className="px-4 py-3 font-mono">
                      <div className="flex items-center gap-1.5">
                        <span
                          className={`px-1.5 py-0.5 rounded text-[10px] font-bold border ${getMethodBadgeClass(
                            m.httpMethod
                          )}`}
                        >
                          {m.httpMethod}
                        </span>
                        <span className="text-slate-300 truncate max-w-xs">{m.endpointPath}</span>
                      </div>
                    </td>

                    <td className="px-4 py-3">
                      <span className={`px-2 py-0.5 rounded-full text-[10px] font-medium border ${getEnvBadge(m.environment)}`}>
                        {m.environment}
                      </span>
                    </td>

                    <td className="px-4 py-3">
                      <StatusBadge status={m.currentStatus} pulse={m.currentStatus === 'DOWN'} />
                    </td>

                    <td className="px-4 py-3 text-slate-300">
                      <div className="flex items-center gap-1 text-[11px]">
                        <Clock className="w-3 h-3 text-slate-400" />
                        <span>{m.monitoringIntervalSeconds}s</span>
                        <span className="text-slate-500">/</span>
                        <span className="text-slate-400">{m.timeoutMs}ms</span>
                      </div>
                    </td>

                    <td className="px-4 py-3">
                      <button
                        onClick={() => toggleMutation.mutate(m.id)}
                        disabled={toggleMutation.isPending}
                        className={`relative inline-flex h-5 w-9 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none ${
                          m.isActive ? 'bg-electric-500' : 'bg-slate-700'
                        }`}
                        title={m.isActive ? 'Click to Pause' : 'Click to Enable'}
                      >
                        <span
                          className={`pointer-events-none inline-block h-4 w-4 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out ${
                            m.isActive ? 'translate-x-4' : 'translate-x-0'
                          }`}
                        />
                      </button>
                    </td>

                    <td className="px-4 py-3 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <Button
                          variant="secondary"
                          size="sm"
                          onClick={() => probeMutation.mutate(m.id)}
                          disabled={probeMutation.isPending}
                          title="Run Immediate Health Check"
                          leftIcon={<Play className="w-3 h-3 text-emerald-400" />}
                        >
                          Probe
                        </Button>
                        <Link to={`/monitors/${m.id}`}>
                          <Button variant="ghost" size="sm">
                            Inspect
                          </Button>
                        </Link>
                        <button
                          onClick={() => setMonitorToDelete(m)}
                          className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded transition-colors"
                          title="Delete Monitor"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      {/* Register New Monitor Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title="Register Monitored API Endpoint"
        subtitle="Configure proactive health checks with automated SSRF protection and latency tracking"
        maxWidth="lg"
      >
        <form onSubmit={handleCreateSubmit} className="space-y-4">
          <div className="bg-electric-500/10 border border-electric-500/20 rounded-lg p-2.5 flex items-start gap-2 text-xs text-electric-300">
            <ShieldCheck className="w-4 h-4 text-electric-400 flex-shrink-0 mt-0.5" />
            <div>
              <strong>SSRF Guard Active:</strong> Probes strictly target approved endpoints. Requests to loopback (127.0.0.1, localhost), link-local, and cloud metadata (169.254.169.254) are rejected at the kernel socket boundary.
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            <Input
              label="Monitor Name *"
              placeholder="e.g. Payments Gateway API"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              required
            />

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Environment *</label>
              <select
                value={formData.environment}
                onChange={(e) => setFormData({ ...formData, environment: e.target.value as EnvironmentType })}
                className="w-full bg-navy-950/60 border border-navy-750 text-xs rounded-lg px-3 py-2 text-slate-200 focus:outline-none focus:border-electric-500"
              >
                <option value="PRODUCTION">Production</option>
                <option value="STAGING">Staging</option>
                <option value="DEVELOPMENT">Development</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div className="md:col-span-1">
              <label className="block text-xs font-medium text-slate-300 mb-1">HTTP Method *</label>
              <select
                value={formData.httpMethod}
                onChange={(e) => setFormData({ ...formData, httpMethod: e.target.value as HttpMethod })}
                className="w-full bg-navy-950/60 border border-navy-750 text-xs rounded-lg px-3 py-2 text-slate-200 focus:outline-none focus:border-electric-500"
              >
                <option value="GET">GET</option>
                <option value="POST">POST</option>
                <option value="PUT">PUT</option>
                <option value="DELETE">DELETE</option>
                <option value="PATCH">PATCH</option>
                <option value="HEAD">HEAD</option>
              </select>
            </div>

            <div className="md:col-span-2">
              <Input
                label="Base URL *"
                placeholder="https://api.example.com"
                value={formData.baseUrl}
                onChange={(e) => setFormData({ ...formData, baseUrl: e.target.value })}
                required
              />
            </div>
          </div>

          <Input
            label="Endpoint Path *"
            placeholder="/api/v1/health"
            value={formData.endpointPath}
            onChange={(e) => setFormData({ ...formData, endpointPath: e.target.value })}
            required
          />

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <Input
              label="Interval (Seconds)"
              type="number"
              min={10}
              max={3600}
              value={formData.monitoringIntervalSeconds}
              onChange={(e) => setFormData({ ...formData, monitoringIntervalSeconds: Number(e.target.value) })}
            />

            <Input
              label="Timeout (ms)"
              type="number"
              min={500}
              max={30000}
              value={formData.timeoutMs}
              onChange={(e) => setFormData({ ...formData, timeoutMs: Number(e.target.value) })}
            />

            <Input
              label="Expected Status"
              type="number"
              min={100}
              max={599}
              value={formData.expectedHttpStatus}
              onChange={(e) => setFormData({ ...formData, expectedHttpStatus: Number(e.target.value) })}
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">Request Headers (JSON)</label>
            <textarea
              rows={2}
              value={formData.requestHeaders}
              onChange={(e) => setFormData({ ...formData, requestHeaders: e.target.value })}
              className="w-full bg-navy-950/60 border border-navy-750 font-mono text-xs rounded-lg p-2 text-slate-200 focus:outline-none focus:border-electric-500"
            />
          </div>

          {['POST', 'PUT', 'PATCH'].includes(formData.httpMethod) && (
            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Request Payload Body (JSON)</label>
              <textarea
                rows={2}
                placeholder='{"ping": "pong"}'
                value={formData.requestBody}
                onChange={(e) => setFormData({ ...formData, requestBody: e.target.value })}
                className="w-full bg-navy-950/60 border border-navy-750 font-mono text-xs rounded-lg p-2 text-slate-200 focus:outline-none focus:border-electric-500"
              />
            </div>
          )}

          <div className="flex items-center justify-end gap-2 pt-3 border-t border-navy-750">
            <Button variant="ghost" type="button" onClick={() => setIsCreateModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" isLoading={createMutation.isPending}>
              Register Monitor
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={!!monitorToDelete}
        onClose={() => setMonitorToDelete(null)}
        title="Confirm Monitor Deletion"
        subtitle="This action cannot be undone and will delete all historical probe records"
        maxWidth="sm"
      >
        <div className="space-y-4">
          <div className="flex items-center gap-3 p-3 bg-rose-500/10 border border-rose-500/20 rounded-lg text-rose-300 text-xs">
            <AlertTriangle className="w-5 h-5 flex-shrink-0 text-rose-400" />
            <div>
              Are you sure you want to remove <strong>{monitorToDelete?.name}</strong>?
            </div>
          </div>
          <div className="flex justify-end gap-2">
            <Button variant="ghost" onClick={() => setMonitorToDelete(null)}>
              Cancel
            </Button>
            <Button
              variant="danger"
              isLoading={deleteMutation.isPending}
              onClick={() => monitorToDelete && deleteMutation.mutate(monitorToDelete.id)}
            >
              Delete Permanently
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
