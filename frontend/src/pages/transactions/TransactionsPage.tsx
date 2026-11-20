import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import {
  Layers,
  Search,
  ExternalLink,
  RefreshCw,
  Plus,
  Clock,
  CheckCircle2,
  AlertCircle,
  Activity,
  Filter,
} from 'lucide-react';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { LoadingSkeleton } from '../../components/ui/LoadingSkeleton';
import { EmptyState } from '../../components/ui/EmptyState';
import { useToast } from '../../context/ToastContext';
import { transactionService } from '../../services/transactionService';
import { Transaction, TransactionStatus } from '../../types/transaction';

export const TransactionsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const toast = useToast();

  const [search, setSearch] = useState('');
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');
  const [page, setPage] = useState(0);

  const {
    data: paginatedData,
    isLoading,
    isFetching,
    refetch,
  } = useQuery({
    queryKey: ['transactions', search, selectedStatus, page],
    queryFn: () =>
      transactionService.getAllTransactions({
        search: search.trim() || undefined,
        status: selectedStatus !== 'ALL' ? (selectedStatus as TransactionStatus) : undefined,
        page,
        size: 15,
      }),
    refetchInterval: 30000,
  });

  // Demo Ingest Transaction Mutation
  const ingestMutation = useMutation({
    mutationFn: async () => {
      const txNum = Math.floor(10000 + Math.random() * 90000);
      const tx = await transactionService.createTransaction({
        transactionId: `TXN-${txNum}-DEMO`,
        correlationId: `CORR-${Math.random().toString(36).substring(2, 10).toUpperCase()}`,
        requestId: `REQ-${Math.random().toString(36).substring(2, 8).toUpperCase()}`,
        transactionStatus: 'PROCESSING',
        rootServiceName: 'api-gateway',
        metadata: '{"source": "demo_synthetic_generator", "env": "production"}',
      });

      // Record 3 steps
      await transactionService.recordStep(tx.id, {
        serviceName: 'api-gateway',
        stepName: 'Validate Auth Token',
        stepOrder: 1,
        status: 'SUCCESS',
        durationMs: 38,
        payloadSnapshot: '{"auth": "verified", "sub": "usr_99182"}',
      });

      await transactionService.recordStep(tx.id, {
        serviceName: 'order-service',
        stepName: 'Create Ledger Entry',
        stepOrder: 2,
        status: 'SUCCESS',
        durationMs: 145,
        payloadSnapshot: '{"order_id": "ord_88219", "items": 2}',
      });

      await transactionService.recordStep(tx.id, {
        serviceName: 'payment-service',
        stepName: 'Charge Payment Method',
        stepOrder: 3,
        status: 'SUCCESS',
        durationMs: 310,
        payloadSnapshot: '{"charge_id": "ch_394819", "amount": 4900}',
      });

      return tx;
    },
    onSuccess: (tx) => {
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      toast.success('Transaction Generated', `Dispatched ${tx.transactionId} across 3 microservices`);
    },
    onError: () => {
      toast.error('Simulation Failed', 'Could not record synthetic transaction');
    },
  });

  const transactions = paginatedData?.content || [];
  const totalElements = paginatedData?.totalElements || 0;
  const totalPages = paginatedData?.totalPages || 0;

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
            <Layers className="w-5 h-5 text-electric-400" /> Distributed Transactions
            {isFetching && <RefreshCw className="w-3.5 h-3.5 text-slate-400 animate-spin" />}
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Trace end-to-end multi-service business workflows, correlation IDs, and execution waterfalls
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
            onClick={() => ingestMutation.mutate()}
            isLoading={ingestMutation.isPending}
            leftIcon={<Plus className="w-4 h-4" />}
          >
            Simulate Transaction
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
              placeholder="Filter by Transaction ID or Correlation ID..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
              className="w-full bg-navy-950/60 border border-navy-750 text-xs rounded-lg pl-9 pr-3 py-2 text-slate-200 placeholder-slate-500 focus:outline-none focus:border-electric-500"
            />
          </div>

          <div className="flex items-center gap-2 w-full md:w-auto">
            <select
              value={selectedStatus}
              onChange={(e) => {
                setSelectedStatus(e.target.value);
                setPage(0);
              }}
              className="bg-navy-950/60 border border-navy-750 text-xs rounded-lg px-2.5 py-2 text-slate-300 focus:outline-none focus:border-electric-500"
            >
              <option value="ALL">All Statuses</option>
              <option value="SUCCESS">Success</option>
              <option value="FAILED">Failed</option>
              <option value="PROCESSING">Processing</option>
              <option value="PENDING">Pending</option>
            </select>
          </div>
        </div>
      </Card>

      {/* Transactions Table */}
      <Card>
        {isLoading ? (
          <div className="p-4 space-y-3">
            <LoadingSkeleton count={8} height="h-10" />
          </div>
        ) : transactions.length === 0 ? (
          <EmptyState
            title="No Transactions Found"
            description={
              search || selectedStatus !== 'ALL'
                ? 'Try loosening your filter parameters.'
                : 'No distributed transaction spans have been ingested yet.'
            }
            action={
              search ? undefined : (
                <Button
                  variant="primary"
                  size="sm"
                  onClick={() => ingestMutation.mutate()}
                  isLoading={ingestMutation.isPending}
                >
                  Simulate Transaction
                </Button>
              )
            }
            icon={<Layers className="w-8 h-8 text-slate-500" />}
          />
        ) : (
          <div>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-navy-750 text-[11px] font-semibold uppercase text-slate-400">
                    <th className="px-4 py-3">Transaction ID</th>
                    <th className="px-4 py-3">Correlation ID</th>
                    <th className="px-4 py-3">Status</th>
                    <th className="px-4 py-3">Root Service</th>
                    <th className="px-4 py-3">Duration</th>
                    <th className="px-4 py-3">Started</th>
                    <th className="px-4 py-3 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-navy-750/50 text-slate-200">
                  {transactions.map((t) => (
                    <tr key={t.id} className="hover:bg-navy-800/40 transition-colors group">
                      <td className="px-4 py-3 font-mono font-medium text-white">
                        <Link
                          to={`/transactions/${t.id}`}
                          className="hover:text-electric-400 flex items-center gap-1.5 transition-colors"
                        >
                          {t.transactionId}
                          <ExternalLink className="w-3 h-3 opacity-0 group-hover:opacity-100 transition-opacity text-slate-400" />
                        </Link>
                      </td>

                      <td className="px-4 py-3 font-mono text-slate-400 text-[11px]">
                        {t.correlationId}
                      </td>

                      <td className="px-4 py-3">
                        <StatusBadge
                          status={
                            t.transactionStatus === 'SUCCESS'
                              ? 'HEALTHY'
                              : t.transactionStatus === 'FAILED'
                              ? 'DOWN'
                              : 'DEGRADED'
                          }
                          pulse={t.transactionStatus === 'PROCESSING'}
                        />
                      </td>

                      <td className="px-4 py-3 font-mono text-slate-300">
                        <span className="px-2 py-0.5 rounded bg-navy-800 border border-navy-750 text-[11px]">
                          {t.rootServiceName || 'gateway'}
                        </span>
                      </td>

                      <td className="px-4 py-3 font-mono">
                        <span
                          className={`font-semibold ${
                            (t.totalDurationMs || 0) > 1000
                              ? 'text-rose-400'
                              : (t.totalDurationMs || 0) > 400
                              ? 'text-amber-400'
                              : 'text-emerald-400'
                          }`}
                        >
                          {t.totalDurationMs || 0} ms
                        </span>
                      </td>

                      <td className="px-4 py-3 text-slate-400 text-[11px]">
                        {new Date(t.startTime).toLocaleTimeString([], {
                          hour: '2-digit',
                          minute: '2-digit',
                          second: '2-digit',
                        })}
                      </td>

                      <td className="px-4 py-3 text-right">
                        <Link to={`/transactions/${t.id}`}>
                          <Button variant="ghost" size="sm">
                            Inspect Waterfall
                          </Button>
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between px-4 py-3 border-t border-navy-750 text-xs text-slate-400">
                <div>
                  Showing {page * 15 + 1} to {Math.min((page + 1) * 15, totalElements)} of {totalElements} transactions
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
    </div>
  );
};
