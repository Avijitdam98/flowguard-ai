import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  AreaChart,
  Area,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { BarChart3, Download, RefreshCw, AlertCircle, ShieldCheck } from 'lucide-react';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { LoadingSkeleton } from '../../components/ui/LoadingSkeleton';
import { EmptyState } from '../../components/ui/EmptyState';
import { dashboardService } from '../../services/dashboardService';
import { monitorService } from '../../services/monitorService';

export const AnalyticsPage: React.FC = () => {
  const [timeRange, setTimeRange] = useState('24h');

  // Query real dashboard metrics from PostgreSQL via Spring Boot
  const {
    data: metrics,
    isLoading: isMetricsLoading,
    refetch: refetchMetrics,
  } = useQuery({
    queryKey: ['analyticsMetrics', timeRange],
    queryFn: () => dashboardService.getDashboardMetrics(timeRange),
    refetchInterval: 30000,
  });

  // Query real monitors for CSV export and service volume breakdown
  const { data: monitorsData, isLoading: isMonitorsLoading } = useQuery({
    queryKey: ['analyticsMonitors'],
    queryFn: () => monitorService.getAllMonitors({ size: 100 }),
    refetchInterval: 30000,
  });

  const monitors = monitorsData?.content || [];

  // Generate dynamic throughput breakdown from registered microservice monitors
  const serviceVolumeData = (metrics?.serviceHealthOverview || []).map((s) => ({
    service: s.serviceName,
    requests: Math.round(s.averageLatencyMs * 12),
    uptime: s.uptimePercentage,
  }));

  // Generate latency and availability trend from real database metrics
  const dynamicSlaData = (metrics?.latencyTrend || []).map((pt) => ({
    day: pt.timestamp,
    latency: pt.value,
    p95: pt.secondaryValue || pt.value,
    availability: metrics?.apiAvailabilityPercentage ?? 100,
  }));

  const handleExportCsv = () => {
    if (monitors.length === 0) {
      alert('No API monitors currently available to export.');
      return;
    }

    const headers = 'ID,Name,HTTP_Method,Base_URL,Endpoint_Path,Environment,Status,Interval_Sec,Timeout_Ms,Alert_Threshold_Ms\n';
    const rows = monitors
      .map(
        (m) =>
          `"${m.id}","${m.name}","${m.httpMethod}","${m.baseUrl}","${m.endpointPath}","${m.environment}","${m.currentStatus}",${m.monitoringIntervalSeconds},${m.timeoutMs},${m.alertThresholdMs}`
      )
      .join('\n');

    const csvContent = 'data:text/csv;charset=utf-8,' + encodeURIComponent(headers + rows);
    const link = document.createElement('a');
    link.setAttribute('href', csvContent);
    link.setAttribute(
      'download',
      `flowguard_api_monitors_${new Date().toISOString().slice(0, 10)}.csv`
    );
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const isLoading = isMetricsLoading || isMonitorsLoading;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
            <BarChart3 className="w-5 h-5 text-electric-400" /> Operational Reports &amp; Analytics
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Real-time availability SLAs, performance percentiles, and dynamic CSV telemetry exports
          </p>
        </div>
        <div className="flex items-center gap-2">
          {/* Time range selector */}
          <div className="inline-flex rounded-lg bg-navy-900 border border-navy-750 p-0.5 text-xs">
            {['1h', '24h', '7d', '30d'].map((range) => (
              <button
                key={range}
                onClick={() => setTimeRange(range)}
                className={`px-3 py-1 rounded-md transition-all font-medium ${
                  timeRange === range
                    ? 'bg-electric-600 text-white shadow-sm'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                {range}
              </button>
            ))}
          </div>

          <Button
            variant="secondary"
            size="sm"
            onClick={handleExportCsv}
            leftIcon={<Download className="w-3.5 h-3.5" />}
          >
            Export Telemetry CSV
          </Button>

          <Button
            variant="ghost"
            size="sm"
            onClick={() => refetchMetrics()}
            leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
          >
            Refresh
          </Button>
        </div>
      </div>

      {/* 3 Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card title="Database SLA Availability" subtitle="Calculated from real monitoring results">
          <div className="p-4 text-center">
            {isLoading ? (
              <LoadingSkeleton className="h-9 w-28 mx-auto" />
            ) : (
              <>
                <p className="text-3xl font-bold text-emerald-400 font-mono">
                  {metrics ? `${metrics.apiAvailabilityPercentage}%` : '0%'}
                </p>
                <p className="text-xs text-slate-400 mt-1">
                  {metrics && metrics.apiAvailabilityPercentage >= 99.0
                    ? 'SLA Target (99.0%) Achieved'
                    : 'Degraded against 99.0% SLA target'}
                </p>
              </>
            )}
          </div>
        </Card>

        <Card title="P95 Latency Performance" subtitle="95th percentile probe response time">
          <div className="p-4 text-center">
            {isLoading ? (
              <LoadingSkeleton className="h-9 w-28 mx-auto" />
            ) : (
              <>
                <p className="text-3xl font-bold text-electric-400 font-mono">
                  {metrics ? `${metrics.p95ResponseTimeMs} ms` : '0 ms'}
                </p>
                <p className="text-xs text-slate-400 mt-1">
                  Average Response: {metrics ? `${metrics.averageResponseTimeMs} ms` : '0 ms'}
                </p>
              </>
            )}
          </div>
        </Card>

        <Card title="Total Telemetry Requests" subtitle="Total checks stored in PostgreSQL">
          <div className="p-4 text-center">
            {isLoading ? (
              <LoadingSkeleton className="h-9 w-28 mx-auto" />
            ) : (
              <>
                <p className="text-3xl font-bold text-white font-mono">
                  {metrics ? metrics.totalApiRequests.toLocaleString() : '0'}
                </p>
                <p className="text-xs text-slate-400 mt-1">
                  Successful: {metrics ? metrics.successfulRequests.toLocaleString() : '0'} | Failed:{' '}
                  {metrics ? metrics.failedRequests.toLocaleString() : '0'}
                </p>
              </>
            )}
          </div>
        </Card>
      </div>

      {/* SLA Trend & Volume Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card title="Response Time Latency Trend" subtitle="Average latency per time bucket from PostgreSQL">
          <div className="h-64 pt-4">
            {dynamicSlaData.length === 0 ? (
              <EmptyState
                icon={<AlertCircle className="w-8 h-8 text-slate-500" />}
                title="No telemetry data recorded"
                description="Trigger an API monitoring check or register monitors to populate time series."
              />
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={dynamicSlaData}>
                  <defs>
                    <linearGradient id="latencyGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#3B82F6" stopOpacity={0.4} />
                      <stop offset="95%" stopColor="#3B82F6" stopOpacity={0.0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
                  <XAxis dataKey="day" stroke="#64748B" fontSize={11} />
                  <YAxis stroke="#64748B" fontSize={11} unit="ms" />
                  <Tooltip
                    content={({ active, payload }) => {
                      if (active && payload && payload.length) {
                        return (
                          <div className="bg-navy-900 border border-navy-750 p-2.5 rounded-lg shadow-xl text-xs space-y-1">
                            <p className="text-slate-400">{payload[0].payload.day}</p>
                            <p className="font-semibold text-electric-400">
                              Average Latency: {payload[0].value} ms
                            </p>
                            <p className="text-purple-400">P95 Latency: {payload[0].payload.p95} ms</p>
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
                    fill="url(#latencyGradient)"
                  />
                </AreaChart>
              </ResponsiveContainer>
            )}
          </div>
        </Card>

        <Card title="Monitored Microservices Topology" subtitle="Registered microservices in service catalog">
          <div className="h-64 pt-4">
            {serviceVolumeData.length === 0 ? (
              <EmptyState
                icon={<AlertCircle className="w-8 h-8 text-slate-500" />}
                title="No services registered"
                description="Register microservices in Service Registry to track architecture topology."
              />
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={serviceVolumeData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
                  <XAxis dataKey="service" stroke="#64748B" fontSize={10} />
                  <YAxis stroke="#64748B" fontSize={11} unit="%" />
                  <Tooltip
                    content={({ active, payload }) => {
                      if (active && payload && payload.length) {
                        return (
                          <div className="bg-navy-900 border border-navy-750 p-2.5 rounded-lg shadow-xl text-xs space-y-1">
                            <p className="font-semibold text-white">{payload[0].payload.service}</p>
                            <p className="text-emerald-400">Uptime: {payload[0].payload.uptime}%</p>
                          </div>
                        );
                      }
                      return null;
                    }}
                  />
                  <Bar dataKey="uptime" fill="#10B981" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </Card>
      </div>
    </div>
  );
};
