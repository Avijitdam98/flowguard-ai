import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Terminal,
  Search,
  RefreshCw,
  Eye,
  Copy,
  Plus,
  AlertTriangle,
  AlertCircle,
  Info,
  Layers,
  ChevronDown,
  ChevronRight,
  Radio,
} from 'lucide-react';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { LoadingSkeleton } from '../../components/ui/LoadingSkeleton';
import { EmptyState } from '../../components/ui/EmptyState';
import { Modal } from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import { logService } from '../../services/logService';
import { LogEntry, LogLevel } from '../../types/log';

export const LogsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const toast = useToast();

  const [search, setSearch] = useState('');
  const [selectedService, setSelectedService] = useState('ALL');
  const [filterLevel, setFilterLevel] = useState<string>('ALL');
  const [selectedLog, setSelectedLog] = useState<LogEntry | null>(null);
  const [page, setPage] = useState(0);
  const [autoRefresh, setAutoRefresh] = useState(true);

  // Query distinct services
  const { data: services = [] } = useQuery({
    queryKey: ['logServices'],
    queryFn: () => logService.getDistinctServices(),
  });

  // Query log stats
  const { data: logStats } = useQuery({
    queryKey: ['logStats'],
    queryFn: () => logService.getLogStats(),
    refetchInterval: autoRefresh ? 10000 : false,
  });

  // Query logs
  const {
    data: logsData,
    isLoading,
    isFetching,
    refetch,
  } = useQuery({
    queryKey: ['logs', selectedService, filterLevel, search, page],
    queryFn: () =>
      logService.searchLogs({
        serviceName: selectedService !== 'ALL' ? selectedService : undefined,
        level: filterLevel !== 'ALL' ? (filterLevel as LogLevel) : undefined,
        search: search.trim() || undefined,
        page,
        size: 25,
      }),
    refetchInterval: autoRefresh ? 5000 : false,
  });

  // Simulate Log Ingestion Mutation
  const simulateMutation = useMutation({
    mutationFn: async () => {
      const mockErrors = [
        'PaymentGatewayTimeout: Downstream gateway did not reply within 4500ms',
        'RedisConnectionFailure: Read timed out after 2000ms connecting to replica',
        'OptimisticLockException: Concurrent record update on order ledger #ord-88192',
        'JwtTokenExpired: Bearer signature validation failed during ingress proxy',
      ];
      const errorMsg = mockErrors[Math.floor(Math.random() * mockErrors.length)];
      return logService.ingestLog({
        serviceName: 'payment-service',
        environment: 'PRODUCTION',
        logLevel: 'ERROR',
        message: errorMsg,
        correlationId: `CORR-${Math.random().toString(36).substring(2, 10).toUpperCase()}`,
        traceId: `TRACE-${Math.random().toString(36).substring(2, 12).toLowerCase()}`,
        exceptionDetails: `com.flowguard.exception.FlowGuardException: ${errorMsg}\n\tat com.flowguard.service.Worker.execute(Worker.java:44)\n\tat java.base/java.lang.Thread.run(Thread.java:1583)`,
        loggerName: 'com.flowguard.payment.ServiceLogger',
        metadata: '{"synthetic": true, "pod": "payment-pod-99"}',
      });
    },
    onSuccess: (newLog) => {
      queryClient.invalidateQueries({ queryKey: ['logs'] });
      queryClient.invalidateQueries({ queryKey: ['logStats'] });
      toast.warning('Log Ingested', `Ingested ${newLog.logLevel} log from ${newLog.serviceName}`);
    },
    onError: () => {
      toast.error('Simulation Failed', 'Could not post synthetic log');
    },
  });

  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    toast.info('Copied', `${label} copied to clipboard`);
  };

  const logs = logsData?.content || [];
  const totalElements = logsData?.totalElements || 0;
  const totalPages = logsData?.totalPages || 0;

  const getLevelBadgeClass = (level: LogLevel) => {
    switch (level) {
      case 'ERROR':
      case 'FATAL':
        return 'bg-rose-500/10 text-rose-400 border-rose-500/30';
      case 'WARN':
        return 'bg-amber-500/10 text-amber-400 border-amber-500/30';
      case 'INFO':
        return 'bg-electric-500/10 text-electric-400 border-electric-500/30';
      case 'DEBUG':
        return 'bg-slate-500/10 text-slate-400 border-slate-500/30';
      default:
        return 'bg-slate-500/10 text-slate-400 border-slate-500/30';
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
            <Terminal className="w-5 h-5 text-electric-400" /> Centralized Log Explorer
            {isFetching && <RefreshCw className="w-3.5 h-3.5 text-slate-400 animate-spin" />}
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Query and inspect multi-service application telemetry, trace correlations, and stack traces
          </p>
        </div>

        <div className="flex items-center gap-2">
          <label className="flex items-center gap-1.5 text-xs text-slate-400 cursor-pointer mr-2 select-none">
            <input
              type="checkbox"
              checked={autoRefresh}
              onChange={(e) => setAutoRefresh(e.target.checked)}
              className="rounded border-navy-700 bg-navy-900 text-electric-500 focus:ring-0"
            />
            <span>Live Stream</span>
          </label>
          <Button
            variant="outline"
            size="sm"
            onClick={() => refetch()}
            leftIcon={<RefreshCw className={`w-3.5 h-3.5 ${isFetching ? 'animate-spin' : ''}`} />}
          >
            Refresh
          </Button>
          <Button
            variant="secondary"
            size="sm"
            onClick={() => simulateMutation.mutate()}
            isLoading={simulateMutation.isPending}
            leftIcon={<Plus className="w-3.5 h-3.5" />}
          >
            Emit Log Event
          </Button>
        </div>
      </div>

      {/* KPI Stats Bar */}
      {logStats && (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <div className="p-3 bg-navy-900/60 border border-navy-750 rounded-xl">
            <span className="text-[11px] text-slate-400">Total Ingested Logs</span>
            <p className="text-lg font-bold text-white mt-0.5">{logStats.totalLogs.toLocaleString()}</p>
          </div>
          <div className="p-3 bg-navy-900/60 border border-navy-750 rounded-xl">
            <span className="text-[11px] text-rose-400">Errors &amp; Fatal</span>
            <p className="text-lg font-bold text-rose-400 mt-0.5">{logStats.errorCount.toLocaleString()}</p>
          </div>
          <div className="p-3 bg-navy-900/60 border border-navy-750 rounded-xl">
            <span className="text-[11px] text-amber-400">Warnings</span>
            <p className="text-lg font-bold text-amber-400 mt-0.5">{logStats.warningCount.toLocaleString()}</p>
          </div>
          <div className="p-3 bg-navy-900/60 border border-navy-750 rounded-xl">
            <span className="text-[11px] text-electric-400">Info Events</span>
            <p className="text-lg font-bold text-electric-400 mt-0.5">{logStats.infoCount.toLocaleString()}</p>
          </div>
        </div>
      )}

      {/* Filter and Search Bar */}
      <Card className="p-3">
        <div className="flex flex-col md:flex-row gap-3 items-center justify-between">
          <div className="relative flex-1 w-full">
            <Search className="absolute left-3 top-2.5 w-4 h-4 text-slate-400" />
            <input
              type="text"
              placeholder="Search logs by keyword, trace ID, correlation ID, or exception..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
              className="w-full bg-navy-950/60 border border-navy-750 text-xs rounded-lg pl-9 pr-3 py-2 text-slate-200 placeholder-slate-500 focus:outline-none focus:border-electric-500"
            />
          </div>

          <div className="flex flex-wrap items-center gap-2 w-full md:w-auto">
            <select
              value={selectedService}
              onChange={(e) => {
                setSelectedService(e.target.value);
                setPage(0);
              }}
              className="bg-navy-950/60 border border-navy-750 text-xs rounded-lg px-2.5 py-2 text-slate-300 focus:outline-none focus:border-electric-500"
            >
              <option value="ALL">All Microservices</option>
              {services.map((svc) => (
                <option key={svc} value={svc}>
                  {svc}
                </option>
              ))}
            </select>

            <div className="flex items-center gap-1 bg-navy-950/80 p-0.5 rounded-lg border border-navy-750">
              {['ALL', 'INFO', 'WARN', 'ERROR'].map((lvl) => (
                <button
                  key={lvl}
                  onClick={() => {
                    setFilterLevel(lvl);
                    setPage(0);
                  }}
                  className={`px-2.5 py-1 rounded text-[11px] font-semibold transition-colors ${
                    filterLevel === lvl
                      ? 'bg-electric-600 text-white shadow-sm'
                      : 'text-slate-400 hover:text-slate-200'
                  }`}
                >
                  {lvl}
                </button>
              ))}
            </div>
          </div>
        </div>
      </Card>

      {/* Logs Table */}
      <Card>
        {isLoading ? (
          <div className="p-4 space-y-3">
            <LoadingSkeleton count={10} height="h-10" />
          </div>
        ) : logs.length === 0 ? (
          <EmptyState
            title="No Log Records Match Query"
            description="Adjust your search query, service scope, or log severity filters."
            icon={<Terminal className="w-8 h-8 text-slate-500" />}
          />
        ) : (
          <div>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs font-mono">
                <thead>
                  <tr className="border-b border-navy-750 text-[11px] font-sans font-semibold uppercase text-slate-400">
                    <th className="px-4 py-3">Timestamp</th>
                    <th className="px-4 py-3">Level</th>
                    <th className="px-4 py-3">Service</th>
                    <th className="px-4 py-3">Message</th>
                    <th className="px-4 py-3">Correlation ID</th>
                    <th className="px-4 py-3 text-right font-sans">Details</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-navy-750/50 text-slate-200">
                  {logs.map((log) => (
                    <tr
                      key={log.id}
                      onClick={() => setSelectedLog(log)}
                      className="hover:bg-navy-800/40 transition-colors cursor-pointer group"
                    >
                      <td className="px-4 py-2.5 text-slate-400 whitespace-nowrap text-[11px]">
                        {new Date(log.timestamp).toLocaleTimeString([], {
                          hour: '2-digit',
                          minute: '2-digit',
                          second: '2-digit',
                        })}
                      </td>

                      <td className="px-4 py-2.5">
                        <span className={`px-2 py-0.5 rounded text-[10px] font-bold border ${getLevelBadgeClass(log.logLevel)}`}>
                          {log.logLevel}
                        </span>
                      </td>

                      <td className="px-4 py-2.5 text-slate-300 font-semibold truncate max-w-[140px]">
                        {log.serviceName}
                      </td>

                      <td className="px-4 py-2.5 text-slate-200 truncate max-w-md text-[11px]">
                        {log.message}
                      </td>

                      <td className="px-4 py-2.5 text-slate-400 text-[11px] truncate max-w-[140px]">
                        {log.correlationId || '-'}
                      </td>

                      <td className="px-4 py-2.5 text-right font-sans">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            setSelectedLog(log);
                          }}
                          className="p-1 hover:bg-navy-700 rounded text-slate-400 hover:text-white transition-colors"
                          title="Inspect Log Entry"
                        >
                          <Eye className="w-3.5 h-3.5" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between px-4 py-3 border-t border-navy-750 text-xs text-slate-400 font-sans">
                <div>
                  Showing {page * 25 + 1} to {Math.min((page + 1) * 25, totalElements)} of {totalElements} logs
                </div>
                <div className="flex items-center gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    disabled={page === 0}
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                  >
                    Previous
                  </Button>
                  <span className="text-slate-300 font-mono">
                    Page {page + 1} of {totalPages}
                  </span>
                  <Button
                    variant="outline"
                    size="sm"
                    disabled={page >= totalPages - 1}
                    onClick={() => setPage((p) => p + 1)}
                  >
                    Next
                  </Button>
                </div>
              </div>
            )}
          </div>
        )}
      </Card>

      {/* Log Detail Modal */}
      <Modal
        isOpen={!!selectedLog}
        onClose={() => setSelectedLog(null)}
        title="Log Event Inspector"
        subtitle={selectedLog ? `${selectedLog.serviceName} • ${selectedLog.logLevel}` : ''}
        maxWidth="xl"
      >
        {selectedLog && (
          <div className="space-y-4 text-xs">
            <div className="grid grid-cols-2 gap-3 bg-navy-950 p-3 rounded-lg border border-navy-800">
              <div>
                <span className="text-slate-400 block text-[11px]">Timestamp</span>
                <span className="text-slate-200 font-mono">{new Date(selectedLog.timestamp).toISOString()}</span>
              </div>
              <div>
                <span className="text-slate-400 block text-[11px]">Environment</span>
                <span className="text-slate-200">{selectedLog.environment}</span>
              </div>
              <div>
                <span className="text-slate-400 block text-[11px]">Correlation ID</span>
                <span className="text-slate-200 font-mono">{selectedLog.correlationId || 'N/A'}</span>
              </div>
              <div>
                <span className="text-slate-400 block text-[11px]">Trace ID</span>
                <span className="text-slate-200 font-mono">{selectedLog.traceId || 'N/A'}</span>
              </div>
            </div>

            <div>
              <span className="text-slate-400 block mb-1 font-semibold">Message:</span>
              <div className="p-3 bg-navy-950 rounded-lg border border-navy-800 font-mono text-slate-100 break-words">
                {selectedLog.message}
              </div>
            </div>

            {selectedLog.exceptionDetails && (
              <div>
                <div className="flex items-center justify-between mb-1">
                  <span className="text-rose-400 font-semibold flex items-center gap-1.5">
                    <AlertCircle className="w-3.5 h-3.5" /> Stack Trace
                  </span>
                  <button
                    onClick={() => copyToClipboard(selectedLog.exceptionDetails!, 'Stack Trace')}
                    className="text-slate-400 hover:text-white flex items-center gap-1 text-[11px]"
                  >
                    <Copy className="w-3 h-3" /> Copy
                  </button>
                </div>
                <pre className="p-3 bg-rose-950/20 border border-rose-500/20 rounded-lg font-mono text-rose-200 text-[11px] overflow-x-auto max-h-60 whitespace-pre-wrap">
                  {selectedLog.exceptionDetails}
                </pre>
              </div>
            )}

            {selectedLog.metadata && (
              <div>
                <span className="text-slate-400 block mb-1 font-semibold">Contextual Metadata:</span>
                <pre className="p-2.5 bg-navy-950 rounded-lg border border-navy-800 font-mono text-slate-300 text-[11px] overflow-x-auto">
                  {selectedLog.metadata}
                </pre>
              </div>
            )}

            <div className="flex justify-end pt-2 border-t border-navy-750">
              <Button variant="ghost" onClick={() => setSelectedLog(null)}>
                Close
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};
