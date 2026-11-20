import React, { useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import {
  ArrowLeft,
  Play,
  Settings,
  RefreshCw,
  Clock,
  ShieldCheck,
  CheckCircle2,
  AlertCircle,
  Code,
  Calendar,
  Zap,
} from 'lucide-react';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { LoadingSkeleton } from '../../components/ui/LoadingSkeleton';
import { EmptyState } from '../../components/ui/EmptyState';
import { Modal } from '../../components/ui/Modal';
import { Input } from '../../components/ui/Input';
import { useToast } from '../../context/ToastContext';
import { monitorService } from '../../services/monitorService';
import { UpdateApiMonitorRequest } from '../../types/monitor';

export const MonitorDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const toast = useToast();

  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editFormData, setEditFormData] = useState<UpdateApiMonitorRequest>({});

  // Query Monitor Details
  const {
    data: monitor,
    isLoading: isMonitorLoading,
    refetch: refetchMonitor,
  } = useQuery({
    queryKey: ['monitor', id],
    queryFn: () => monitorService.getMonitorById(id!),
    enabled: !!id,
    refetchInterval: 15000,
  });

  // Query History
  const {
    data: historyData,
    isLoading: isHistoryLoading,
    refetch: refetchHistory,
  } = useQuery({
    queryKey: ['monitorHistory', id],
    queryFn: () => monitorService.getMonitorHistory(id!, { size: 30 }),
    enabled: !!id,
    refetchInterval: 15000,
  });

  // Probe Mutation
  const probeMutation = useMutation({
    mutationFn: () => monitorService.triggerProbe(id!),
    onSuccess: (res) => {
      queryClient.invalidateQueries({ queryKey: ['monitor', id] });
      queryClient.invalidateQueries({ queryKey: ['monitorHistory', id] });
      queryClient.invalidateQueries({ queryKey: ['monitors'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      if (res.isSuccessful) {
        toast.success('Probe Succeeded', `HTTP ${res.statusCode} recorded in ${res.responseTimeMs}ms`);
      } else {
        toast.warning('Probe Alert', `HTTP ${res.statusCode}: ${res.errorMessage || 'Failure'}`);
      }
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to execute probe';
      toast.error('Probe Failed', msg);
    },
  });

  // Update Mutation
  const updateMutation = useMutation({
    mutationFn: (data: UpdateApiMonitorRequest) => monitorService.updateMonitor(id!, data),
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ['monitor', id] });
      queryClient.invalidateQueries({ queryKey: ['monitors'] });
      toast.success('Monitor Updated', `${updated.name} settings saved`);
      setIsEditModalOpen(false);
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to update monitor';
      toast.error('Update Failed', msg);
    },
  });

  const openEditModal = () => {
    if (!monitor) return;
    setEditFormData({
      name: monitor.name,
      baseUrl: monitor.baseUrl,
      endpointPath: monitor.endpointPath,
      httpMethod: monitor.httpMethod,
      environment: monitor.environment,
      monitoringIntervalSeconds: monitor.monitoringIntervalSeconds,
      timeoutMs: monitor.timeoutMs,
      expectedHttpStatus: monitor.expectedHttpStatus,
      alertThresholdMs: monitor.alertThresholdMs,
      requestHeaders: monitor.requestHeaders,
      requestBody: monitor.requestBody,
      isActive: monitor.isActive,
    });
    setIsEditModalOpen(true);
  };

  const handleEditSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateMutation.mutate(editFormData);
  };

  if (isMonitorLoading) {
    return (
      <div className="space-y-6">
        <LoadingSkeleton className="h-16" />
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <LoadingSkeleton className="h-28" />
          <LoadingSkeleton className="h-28" />
          <LoadingSkeleton className="h-28" />
          <LoadingSkeleton className="h-28" />
        </div>
        <LoadingSkeleton className="h-64" />
      </div>
    );
  }

  if (!monitor) {
    return (
      <EmptyState
        title="Monitor Not Found"
        description="The requested API monitor could not be located or may have been deleted."
        action={
          <Button variant="primary" size="sm" onClick={() => navigate('/api-monitors')}>
            Back to Monitors
          </Button>
        }
      />
    );
  }

  const results = historyData?.content || [];
  const chartData = [...results].reverse().map((r) => ({
    time: new Date(r.checkedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
    latency: r.responseTimeMs,
    status: r.statusCode,
    success: r.isSuccessful,
  }));

  const totalChecks = results.length;
  const successfulChecks = results.filter((r) => r.isSuccessful).length;
  const uptimeRate = totalChecks > 0 ? ((successfulChecks / totalChecks) * 100).toFixed(1) : '100.0';
  const avgLatency =
    totalChecks > 0 ? Math.round(results.reduce((acc, r) => acc + r.responseTimeMs, 0) / totalChecks) : 0;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <Link to="/api-monitors">
            <Button variant="secondary" size="sm" leftIcon={<ArrowLeft className="w-3.5 h-3.5" />}>
              Back
            </Button>
          </Link>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold tracking-tight text-white">{monitor.name}</h1>
              <StatusBadge status={monitor.currentStatus} pulse={monitor.currentStatus === 'DOWN'} />
            </div>
            <p className="text-xs font-mono text-slate-400 mt-0.5 flex items-center gap-2">
              <span className="font-semibold text-electric-400">{monitor.httpMethod}</span>
              <span>{monitor.fullUrl}</span>
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => {
              refetchMonitor();
              refetchHistory();
            }}
            leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
          >
            Refresh
          </Button>
          <Button
            variant="secondary"
            size="sm"
            onClick={openEditModal}
            leftIcon={<Settings className="w-3.5 h-3.5" />}
          >
            Configure
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={() => probeMutation.mutate()}
            isLoading={probeMutation.isPending}
            leftIcon={<Play className="w-3.5 h-3.5" />}
          >
            Run Probe Now
          </Button>
        </div>
      </div>

      {/* 4 KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="p-4">
          <span className="text-xs text-slate-400 font-medium">Health Status</span>
          <div className="flex items-center justify-between mt-1">
            <span
              className={`text-xl font-bold ${monitor.currentStatus === 'HEALTHY'
                ? 'text-emerald-400'
                : monitor.currentStatus === 'DEGRADED'
                  ? 'text-amber-400'
                  : 'text-rose-400'
                }`}
            >
              {monitor.currentStatus}
            </span>
            <Zap className="w-5 h-5 text-slate-500" />
          </div>
          <p className="text-[11px] text-slate-400 mt-1">
            {monitor.isActive ? `Probing every ${monitor.monitoringIntervalSeconds}s` : 'Probing paused'}
          </p>
        </Card>

        <Card className="p-4">
          <span className="text-xs text-slate-400 font-medium">Average Response Time</span>
          <div className="flex items-center justify-between mt-1">
            <span className="text-xl font-bold text-white">{avgLatency} ms</span>
            <Clock className="w-5 h-5 text-slate-500" />
          </div>
          <p className={`text-[11px] mt-1 ${avgLatency > monitor.alertThresholdMs ? 'text-rose-400' : 'text-slate-400'}`}>
            Threshold: {monitor.alertThresholdMs} ms
          </p>
        </Card>

        <Card className="p-4">
          <span className="text-xs text-slate-400 font-medium">Uptime Availability</span>
          <div className="flex items-center justify-between mt-1">
            <span className="text-xl font-bold text-emerald-400">{uptimeRate}%</span>
            <CheckCircle2 className="w-5 h-5 text-emerald-500/60" />
          </div>
          <p className="text-[11px] text-slate-400 mt-1">Based on recent {totalChecks} probe checks</p>
        </Card>

        <Card className="p-4">
          <span className="text-xs text-slate-400 font-medium">Expected Response</span>
          <div className="flex items-center justify-between mt-1">
            <span className="text-xl font-bold text-electric-400">HTTP {monitor.expectedHttpStatus}</span>
            <ShieldCheck className="w-5 h-5 text-electric-500/60" />
          </div>
          <p className="text-[11px] text-slate-400 mt-1">Timeout cutoff: {monitor.timeoutMs} ms</p>
        </Card>
      </div>

      {/* Latency History Chart */}
      <Card
        title="Probe Latency Timeline"
        subtitle="Historical response latency and probe anomalies across consecutive health checks"
      >
        <div className="h-64 w-full pt-4">
          {chartData.length === 0 ? (
            <div className="h-full flex items-center justify-center text-xs text-slate-500">
              No probe telemetry recorded yet. Click "Run Probe Now" to trigger a health check.
            </div>
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartData} margin={{ top: 10, right: 20, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="latencyGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3B82F6" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#3B82F6" stopOpacity={0.0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
                <XAxis dataKey="time" stroke="#64748B" fontSize={11} />
                <YAxis stroke="#64748B" fontSize={11} unit="ms" />
                <Tooltip
                  content={({ active, payload }) => {
                    if (active && payload && payload.length) {
                      const data = payload[0].payload;
                      return (
                        <div className="bg-navy-900 border border-navy-750 p-2.5 rounded-lg shadow-xl text-xs space-y-1">
                          <p className="text-slate-400">{data.time}</p>
                          <p className="font-semibold text-white">Latency: {data.latency} ms</p>
                          <p className={data.success ? 'text-emerald-400' : 'text-rose-400'}>
                            Status Code: {data.status}
                          </p>
                        </div>
                      );
                    }
                    return null;
                  }}
                />
                <Area
                  type="monotone"
                  dataKey="latency"
                  stroke="#3B82F6"
                  strokeWidth={2}
                  fillOpacity={1}
                  fill="url(#latencyGradient)"
                />
              </AreaChart>
            </ResponsiveContainer>
          )}
        </div>
      </Card>

      {/* Configuration & Probe History Tabs/Columns */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left: Configuration Details */}
        <div className="space-y-4">
          <Card title="Probe Configuration" subtitle="SSRF parameters and HTTP headers">
            <div className="space-y-3 text-xs">
              <div className="flex justify-between py-1.5 border-b border-navy-750">
                <span className="text-slate-400">Environment</span>
                <span className="font-medium text-slate-200">{monitor.environment}</span>
              </div>
              <div className="flex justify-between py-1.5 border-b border-navy-750">
                <span className="text-slate-400">Method</span>
                <span className="font-mono font-semibold text-electric-400">{monitor.httpMethod}</span>
              </div>
              <div className="flex justify-between py-1.5 border-b border-navy-750">
                <span className="text-slate-400">Interval</span>
                <span className="font-medium text-slate-200">{monitor.monitoringIntervalSeconds} seconds</span>
              </div>
              <div className="flex justify-between py-1.5 border-b border-navy-750">
                <span className="text-slate-400">Socket Timeout</span>
                <span className="font-medium text-slate-200">{monitor.timeoutMs} ms</span>
              </div>
              <div className="flex justify-between py-1.5 border-b border-navy-750">
                <span className="text-slate-400">Alert Latency Threshold</span>
                <span className="font-medium text-amber-400">&gt; {monitor.alertThresholdMs} ms</span>
              </div>
              <div className="flex justify-between py-1.5 border-b border-navy-750">
                <span className="text-slate-400">Last Verified</span>
                <span className="text-slate-300">
                  {monitor.lastCheckedAt ? new Date(monitor.lastCheckedAt).toLocaleString() : 'Never'}
                </span>
              </div>

              {monitor.requestHeaders && (
                <div className="pt-2">
                  <span className="text-slate-400 block mb-1 font-medium">Configured Headers</span>
                  <pre className="bg-navy-950 p-2 rounded text-[11px] font-mono text-slate-300 overflow-x-auto border border-navy-800">
                    {monitor.requestHeaders}
                  </pre>
                </div>
              )}

              {monitor.requestBody && (
                <div className="pt-1">
                  <span className="text-slate-400 block mb-1 font-medium">Request Payload</span>
                  <pre className="bg-navy-950 p-2 rounded text-[11px] font-mono text-slate-300 overflow-x-auto border border-navy-800 max-h-32">
                    {monitor.requestBody}
                  </pre>
                </div>
              )}
            </div>
          </Card>
        </div>

        {/* Right: Detailed Execution History Table */}
        <div className="lg:col-span-2">
          <Card title="Execution Logs" subtitle={`Showing the last ${results.length} health check probes`}>
            {isHistoryLoading ? (
              <div className="p-4 space-y-2">
                <LoadingSkeleton count={5} height="h-8" />
              </div>
            ) : results.length === 0 ? (
              <div className="p-8 text-center text-xs text-slate-400">
                No probe logs available. Click "Run Probe Now" to trigger an execution.
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead>
                    <tr className="border-b border-navy-750 text-[11px] font-semibold uppercase text-slate-400">
                      <th className="px-4 py-2.5">Time</th>
                      <th className="px-4 py-2.5">Status Code</th>
                      <th className="px-4 py-2.5">Latency</th>
                      <th className="px-4 py-2.5">Result</th>
                      <th className="px-4 py-2.5">Diagnostics</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-navy-750/50 text-slate-200">
                    {results.map((r) => (
                      <tr key={r.id} className="hover:bg-navy-800/40">
                        <td className="px-4 py-2.5 text-slate-400 whitespace-nowrap">
                          {new Date(r.checkedAt).toLocaleTimeString()}
                        </td>
                        <td className="px-4 py-2.5 font-mono font-medium">
                          <span
                            className={`px-1.5 py-0.5 rounded text-[11px] ${r.statusCode >= 200 && r.statusCode < 300
                              ? 'bg-emerald-500/10 text-emerald-400'
                              : 'bg-rose-500/10 text-rose-400'
                              }`}
                          >
                            {r.statusCode}
                          </span>
                        </td>
                        <td className="px-4 py-2.5 font-mono text-slate-300">{r.responseTimeMs} ms</td>
                        <td className="px-4 py-2.5">
                          {r.isSuccessful ? (
                            <span className="text-emerald-400 flex items-center gap-1 text-[11px]">
                              <CheckCircle2 className="w-3.5 h-3.5" /> Pass
                            </span>
                          ) : (
                            <span className="text-rose-400 flex items-center gap-1 text-[11px]">
                              <AlertCircle className="w-3.5 h-3.5" /> Failed
                            </span>
                          )}
                        </td>
                        <td className="px-4 py-2.5 text-slate-400 truncate max-w-xs text-[11px]">
                          {r.errorMessage || r.responseBodySample || 'Healthy'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </Card>
        </div>
      </div>

      {/* Edit Configuration Modal */}
      <Modal
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        title="Update Monitor Configuration"
        subtitle={`Modifying ${monitor.name}`}
        maxWidth="lg"
      >
        <form onSubmit={handleEditSubmit} className="space-y-4">
          <Input
            label="Monitor Name"
            value={editFormData.name || ''}
            onChange={(e) => setEditFormData({ ...editFormData, name: e.target.value })}
            required
          />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <Input
              label="Base URL"
              value={editFormData.baseUrl || ''}
              onChange={(e) => setEditFormData({ ...editFormData, baseUrl: e.target.value })}
              required
            />
            <Input
              label="Endpoint Path"
              value={editFormData.endpointPath || ''}
              onChange={(e) => setEditFormData({ ...editFormData, endpointPath: e.target.value })}
              required
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <Input
              label="Interval (Seconds)"
              type="number"
              value={editFormData.monitoringIntervalSeconds || 60}
              onChange={(e) =>
                setEditFormData({ ...editFormData, monitoringIntervalSeconds: Number(e.target.value) })
              }
            />
            <Input
              label="Timeout (ms)"
              type="number"
              value={editFormData.timeoutMs || 5000}
              onChange={(e) => setEditFormData({ ...editFormData, timeoutMs: Number(e.target.value) })}
            />
            <Input
              label="Alert Threshold (ms)"
              type="number"
              value={editFormData.alertThresholdMs || 2000}
              onChange={(e) => setEditFormData({ ...editFormData, alertThresholdMs: Number(e.target.value) })}
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">Request Headers (JSON)</label>
            <textarea
              rows={3}
              value={editFormData.requestHeaders || ''}
              onChange={(e) => setEditFormData({ ...editFormData, requestHeaders: e.target.value })}
              className="w-full bg-navy-950/60 border border-navy-750 font-mono text-xs rounded-lg p-2 text-slate-200 focus:outline-none focus:border-electric-500"
            />
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-navy-750">
            <Button variant="ghost" type="button" onClick={() => setIsEditModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" isLoading={updateMutation.isPending}>
              Save Changes
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
