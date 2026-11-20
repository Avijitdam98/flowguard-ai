import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Activity,
  CheckCircle2,
  AlertTriangle,
  Clock,
  Gauge,
  ShieldCheck,
  AlertOctagon,
  Layers,
  RefreshCw,
  ExternalLink,
  ArrowUpRight,
  ArrowDownRight,
  TrendingUp,
} from 'lucide-react';
import { dashboardService } from '../../services/dashboardService';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { LoadingSkeleton } from '../../components/ui/LoadingSkeleton';
import { MetricsChart } from '../../components/charts/MetricsChart';
import { Link } from 'react-router-dom';

export const DashboardPage: React.FC = () => {
  const [timeRange, setTimeRange] = useState('24h');

  const {
    data: metrics,
    isLoading,
    isError,
    refetch,
    isFetching,
  } = useQuery({
    queryKey: ['dashboardMetrics', timeRange],
    queryFn: () => dashboardService.getDashboardMetrics(timeRange),
    refetchInterval: 30000, // Refresh every 30s
  });

  if (isError) {
    return (
      <div className="flex flex-col items-center justify-center p-12 text-center rounded-xl border border-rose-500/30 bg-rose-950/20">
        <AlertOctagon className="w-10 h-10 text-rose-400 mb-3" />
        <h3 className="text-base font-semibold text-rose-200">Failed to load dashboard metrics</h3>
        <p className="text-xs text-slate-400 max-w-md mt-1 mb-4">
          The telemetry service could not be reached. Ensure the backend cluster is running.
        </p>
        <Button variant="secondary" size="sm" onClick={() => refetch()} leftIcon={<RefreshCw className="w-3.5 h-3.5" />}>
          Retry Connection
        </Button>
      </div>
    );
  }

  const totalReq = metrics?.totalApiRequests || 0;
  const succReq = metrics?.successfulRequests || 0;
  const failReq = metrics?.failedRequests || 0;

  const kpis = [
    {
      title: 'Total API Requests',
      value: metrics ? metrics.totalApiRequests.toLocaleString() : '0',
      sublabel: totalReq > 0 ? 'Live Telemetry' : 'No checks recorded',
      isPositive: true,
      icon: Activity,
      color: 'text-electric-400',
      bgColor: 'bg-electric-500/10',
    },
    {
      title: 'Successful Requests',
      value: metrics ? metrics.successfulRequests.toLocaleString() : '0',
      sublabel: totalReq > 0 ? `${Math.round((succReq / totalReq) * 1000) / 10}% of total` : '0%',
      isPositive: true,
      icon: CheckCircle2,
      color: 'text-emerald-400',
      bgColor: 'bg-emerald-500/10',
    },
    {
      title: 'Failed Requests',
      value: metrics ? metrics.failedRequests.toLocaleString() : '0',
      sublabel: totalReq > 0 ? `${Math.round((failReq / totalReq) * 1000) / 10}% error rate` : '0%',
      isPositive: failReq === 0,
      isWarning: failReq > 0,
      icon: AlertTriangle,
      color: 'text-rose-400',
      bgColor: 'bg-rose-500/10',
    },
    {
      title: 'Avg Response Time',
      value: metrics && metrics.averageResponseTimeMs > 0 ? `${metrics.averageResponseTimeMs} ms` : '0 ms',
      sublabel: metrics && metrics.averageResponseTimeMs > 0 ? 'Database aggregate' : 'No checks',
      isPositive: true,
      icon: Clock,
      color: 'text-amber-400',
      bgColor: 'bg-amber-500/10',
    },
    {
      title: 'P95 Latency',
      value: metrics && metrics.p95ResponseTimeMs > 0 ? `${metrics.p95ResponseTimeMs} ms` : '0 ms',
      sublabel: metrics && metrics.p95ResponseTimeMs > 0 ? 'P95 SLA threshold' : 'No checks',
      isPositive: metrics ? metrics.p95ResponseTimeMs < 1000 : true,
      icon: Gauge,
      color: 'text-purple-400',
      bgColor: 'bg-purple-500/10',
    },
    {
      title: 'API Availability',
      value: metrics ? `${metrics.apiAvailabilityPercentage}%` : '0%',
      sublabel: metrics && metrics.apiAvailabilityPercentage >= 99.0 ? 'SLA Compliant' : 'Monitored SLAs',
      isPositive: metrics ? metrics.apiAvailabilityPercentage >= 99.0 : true,
      icon: ShieldCheck,
      color: 'text-emerald-400',
      bgColor: 'bg-emerald-500/10',
    },
    {
      title: 'Active Incidents',
      value: metrics ? metrics.activeIncidents.toString() : '0',
      sublabel: metrics && metrics.activeIncidents > 0 ? `${metrics.activeIncidents} open in DB` : 'All systems clear',
      isWarning: metrics ? metrics.activeIncidents > 0 : false,
      icon: AlertOctagon,
      color: 'text-rose-400',
      bgColor: 'bg-rose-500/10',
    },
    {
      title: 'Transaction Success',
      value: metrics ? `${metrics.transactionSuccessRate}%` : '0%',
      sublabel: metrics ? 'Real trace metrics' : 'No traces',
      isPositive: metrics ? metrics.transactionSuccessRate >= 95.0 : true,
      icon: Layers,
      color: 'text-electric-400',
      bgColor: 'bg-electric-500/10',
    },
  ];

  const serviceDistributionData = metrics?.serviceHealthDistribution
    ? Object.entries(metrics.serviceHealthDistribution).map(([name, value]) => ({
        name,
        value,
      }))
    : [];

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Top Controls Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-navy-750/70">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
            Executive Observability Command Center
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Real-time telemetry, transaction flows, microservice health, and automated incident status
          </p>
        </div>

        <div className="flex items-center gap-2.5">
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
            onClick={() => refetch()}
            isLoading={isFetching}
            leftIcon={<RefreshCw className={`w-3.5 h-3.5 ${isFetching ? 'animate-spin' : ''}`} />}
          >
            Refresh
          </Button>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {kpis.map((kpi, idx) => {
          const Icon = kpi.icon;
          return (
            <div
              key={idx}
              className="bg-navy-900 border border-navy-750/80 rounded-xl p-4 shadow-lg hover:border-navy-600 transition-all flex flex-col justify-between"
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-medium text-slate-400">{kpi.title}</span>
                <div className={`p-2 rounded-lg ${kpi.bgColor} ${kpi.color}`}>
                  <Icon className="w-4 h-4" />
                </div>
              </div>
              <div className="mt-3">
                {isLoading ? (
                  <LoadingSkeleton height="h-7" />
                ) : (
                  <div className="flex items-baseline justify-between">
                    <span className="text-xl font-bold text-white tracking-tight font-sans">
                      {kpi.value}
                    </span>
                    <span
                      className={`text-[11px] font-medium flex items-center gap-0.5 ${
                        kpi.isWarning
                          ? 'text-rose-400'
                          : kpi.isPositive
                          ? 'text-emerald-400'
                          : 'text-amber-400'
                      }`}
                    >
                      {kpi.isPositive && !kpi.isWarning && <ArrowUpRight className="w-3 h-3" />}
                      {!kpi.isPositive && !kpi.isWarning && <ArrowDownRight className="w-3 h-3" />}
                      {kpi.sublabel}
                    </span>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Telemetry Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* API Traffic Trend */}
        <Card
          title="API Traffic Velocity (req/h)"
          subtitle="Real-time volume comparing successful transactions vs failed responses"
          action={
            <div className="flex items-center gap-3 text-xs">
              <span className="flex items-center gap-1.5 text-slate-300">
                <span className="w-2.5 h-2.5 rounded-full bg-electric-500" /> Success
              </span>
              <span className="flex items-center gap-1.5 text-slate-300">
                <span className="w-2.5 h-2.5 rounded-full bg-rose-500" /> Failures
              </span>
            </div>
          }
        >
          {isLoading ? (
            <LoadingSkeleton height="h-64" />
          ) : (
            <MetricsChart
              type="area"
              data={metrics?.trafficTrend || []}
              dataKey="value"
              secondaryDataKey="secondaryValue"
              name="Success"
              secondaryName="Failures"
              color="#3B82F6"
              secondaryColor="#EF4444"
              height={260}
            />
          )}
        </Card>

        {/* Response Time Trends */}
        <Card
          title="Response Time Trends (ms)"
          subtitle="Average latency vs P95 degradation thresholds over time"
          action={
            <div className="flex items-center gap-3 text-xs">
              <span className="flex items-center gap-1.5 text-slate-300">
                <span className="w-2.5 h-2.5 rounded-full bg-amber-400" /> Avg Latency
              </span>
              <span className="flex items-center gap-1.5 text-slate-300">
                <span className="w-2.5 h-2.5 rounded-full bg-purple-400" /> P95 Latency
              </span>
            </div>
          }
        >
          {isLoading ? (
            <LoadingSkeleton height="h-64" />
          ) : (
            <MetricsChart
              type="line"
              data={metrics?.latencyTrend || []}
              dataKey="value"
              secondaryDataKey="secondaryValue"
              name="Avg Latency"
              secondaryName="P95 Latency"
              color="#F59E0B"
              secondaryColor="#A855F7"
              height={260}
            />
          )}
        </Card>
      </div>

      {/* Secondary Charts & Top Failing Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Error Rate Distribution */}
        <Card
          title="Error Rate Trend (%)"
          subtitle="Percentage of failed requests per hour"
          className="lg:col-span-2"
        >
          {isLoading ? (
            <LoadingSkeleton height="h-56" />
          ) : (
            <MetricsChart
              type="bar"
              data={metrics?.errorRateTrend || []}
              dataKey="value"
              name="Error Rate (%)"
              color="#EF4444"
              height={220}
            />
          )}
        </Card>

        {/* Service Health Distribution */}
        <Card
          title="Microservice Health Status"
          subtitle="Distribution of registered service states"
        >
          {isLoading ? (
            <LoadingSkeleton height="h-56" />
          ) : (
            <MetricsChart
              type="pie"
              data={serviceDistributionData}
              height={220}
            />
          )}
        </Card>
      </div>

      {/* Operational Overviews Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Service Health Overview */}
        <Card
          title="Microservice Health Overview"
          subtitle="Live status, average latency, and uptime across the topology"
          action={
            <Link
              to="/services"
              className="text-xs text-electric-400 hover:text-electric-300 flex items-center gap-1"
            >
              Topology Graph <ExternalLink className="w-3 h-3" />
            </Link>
          }
        >
          <div className="divide-y divide-navy-750/60 overflow-hidden">
            {isLoading ? (
              <LoadingSkeleton count={5} height="h-10" />
            ) : (
              metrics?.serviceHealthOverview.map((svc) => (
                <div key={svc.id} className="py-2.5 flex items-center justify-between text-xs">
                  <div className="flex items-center gap-3">
                    <StatusBadge status={svc.healthStatus} size="sm" pulse={svc.healthStatus === 'DEGRADED'} />
                    <div>
                      <p className="font-semibold text-slate-200">{svc.serviceName}</p>
                      <p className="text-[10px] text-slate-400">Heartbeat: {svc.lastHeartbeat}</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="font-mono text-slate-200">{svc.averageLatencyMs} ms</p>
                    <p className="text-[10px] text-emerald-400 font-mono">{svc.uptimePercentage}% uptime</p>
                  </div>
                </div>
              ))
            )}
          </div>
        </Card>

        {/* Top Failing APIs */}
        <Card
          title="Top Failing Endpoints"
          subtitle="APIs experiencing highest failure frequencies and HTTP error codes"
          action={
            <Link
              to="/api-monitors"
              className="text-xs text-electric-400 hover:text-electric-300 flex items-center gap-1"
            >
              All Monitors <ExternalLink className="w-3 h-3" />
            </Link>
          }
        >
          <div className="divide-y divide-navy-750/60 overflow-hidden">
            {isLoading ? (
              <LoadingSkeleton count={4} height="h-12" />
            ) : (
              metrics?.topFailingApis.map((api) => (
                <div key={api.id} className="py-2.5 flex items-center justify-between text-xs">
                  <div className="min-w-0 pr-4">
                    <div className="flex items-center gap-2">
                      <span className="px-1.5 py-0.5 rounded text-[10px] font-mono font-bold bg-navy-800 text-slate-300 border border-navy-700">
                        {api.method}
                      </span>
                      <p className="font-medium text-slate-200 truncate">{api.name}</p>
                    </div>
                    <p className="text-[11px] font-mono text-rose-400 mt-1 truncate">
                      HTTP {api.lastStatusCode}: {api.lastErrorMessage}
                    </p>
                  </div>
                  <div className="text-right flex-shrink-0">
                    <span className="px-2 py-0.5 rounded-full text-xs font-mono font-bold bg-rose-500/10 border border-rose-500/30 text-rose-400">
                      {api.failureCount} errors ({api.failureRate}%)
                    </span>
                  </div>
                </div>
              ))
            )}
          </div>
        </Card>
      </div>

      {/* Incidents & Transactions Activity Row */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Active Incidents */}
        <Card
          title="Recent Operational Incidents"
          subtitle="Active incidents undergoing diagnostic triage and resolution"
          action={
            <Link
              to="/incidents"
              className="text-xs text-electric-400 hover:text-electric-300 flex items-center gap-1"
            >
              Incident Command <ExternalLink className="w-3 h-3" />
            </Link>
          }
        >
          <div className="divide-y divide-navy-750/60">
            {isLoading ? (
              <LoadingSkeleton count={3} height="h-12" />
            ) : (
              metrics?.recentIncidents.map((inc) => (
                <div key={inc.id} className="py-2.5 flex items-start justify-between text-xs gap-3">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-mono font-semibold text-slate-400">{inc.incidentNumber}</span>
                      <StatusBadge status={inc.severity} size="sm" />
                      <StatusBadge status={inc.status} size="sm" />
                    </div>
                    <p className="font-medium text-slate-200 mt-1">{inc.title}</p>
                    <p className="text-[10px] text-slate-400 mt-0.5">
                      Affected: <span className="text-slate-300 font-semibold">{inc.affectedService}</span> • {inc.createdAt}
                    </p>
                  </div>
                  <Link
                    to={`/incidents/${inc.id}`}
                    className="p-1 rounded text-slate-400 hover:text-electric-400 hover:bg-navy-800 transition-colors"
                  >
                    <ExternalLink className="w-4 h-4" />
                  </Link>
                </div>
              ))
            )}
          </div>
        </Card>

        {/* Recent Distributed Transactions */}
        <Card
          title="Recent Distributed Transactions"
          subtitle="Multi-hop transaction executions traced across the microservice topology"
          action={
            <Link
              to="/transactions"
              className="text-xs text-electric-400 hover:text-electric-300 flex items-center gap-1"
            >
              Transaction Explorer <ExternalLink className="w-3 h-3" />
            </Link>
          }
        >
          <div className="divide-y divide-navy-750/60">
            {isLoading ? (
              <LoadingSkeleton count={4} height="h-11" />
            ) : (
              metrics?.recentTransactions.map((tx) => (
                <div key={tx.id} className="py-2.5 flex items-center justify-between text-xs">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-mono text-slate-200 font-semibold">{tx.transactionId}</span>
                      <StatusBadge status={tx.status} size="sm" />
                    </div>
                    <p className="text-[10px] font-mono text-slate-400 mt-0.5">
                      Trace: {tx.correlationId} • Root: {tx.rootService}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="font-mono text-slate-200 font-medium">{tx.durationMs} ms</p>
                    <p className="text-[10px] text-slate-400">{tx.startTime}</p>
                  </div>
                </div>
              ))
            )}
          </div>
        </Card>
      </div>
    </div>
  );
};

