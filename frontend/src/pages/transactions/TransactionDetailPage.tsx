import React, { useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  ArrowLeft,
  CheckCircle2,
  XCircle,
  Clock,
  Copy,
  Layers,
  ChevronDown,
  ChevronRight,
  AlertTriangle,
  Server,
  Code,
  Calendar,
  Share2,
} from 'lucide-react';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { LoadingSkeleton } from '../../components/ui/LoadingSkeleton';
import { EmptyState } from '../../components/ui/EmptyState';
import { useToast } from '../../context/ToastContext';
import { transactionService } from '../../services/transactionService';
import { TransactionStep } from '../../types/transaction';

export const TransactionDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const toast = useToast();

  const [expandedStepId, setExpandedStepId] = useState<string | null>(null);

  const {
    data: transaction,
    isLoading,
    refetch,
  } = useQuery({
    queryKey: ['transaction', id],
    queryFn: () => transactionService.getTransactionById(id!),
    enabled: !!id,
  });

  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    toast.info('Copied', `${label} copied to clipboard`);
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <LoadingSkeleton className="h-16" />
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <LoadingSkeleton className="h-28" />
          <LoadingSkeleton className="h-28" />
          <LoadingSkeleton className="h-28" />
          <LoadingSkeleton className="h-28" />
        </div>
        <LoadingSkeleton className="h-96" />
      </div>
    );
  }

  if (!transaction) {
    return (
      <EmptyState
        title="Transaction Not Found"
        description="The requested distributed transaction record could not be found."
        action={
          <Button variant="primary" size="sm" onClick={() => navigate('/transactions')}>
            Back to Transactions
          </Button>
        }
      />
    );
  }

  const steps = transaction.steps || [];
  const totalDuration = transaction.totalDurationMs || steps.reduce((sum, s) => sum + s.durationMs, 0) || 1;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <Link to="/transactions">
            <Button variant="secondary" size="sm" leftIcon={<ArrowLeft className="w-3.5 h-3.5" />}>
              Back
            </Button>
          </Link>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
                Transaction: <span className="font-mono text-electric-400">{transaction.transactionId}</span>
              </h1>
              <StatusBadge
                status={
                  transaction.transactionStatus === 'SUCCESS'
                    ? 'HEALTHY'
                    : transaction.transactionStatus === 'FAILED'
                    ? 'DOWN'
                    : 'DEGRADED'
                }
              />
            </div>
            <div className="flex items-center gap-3 text-xs font-mono text-slate-400 mt-1">
              <span>Correlation: {transaction.correlationId}</span>
              <button
                onClick={() => copyToClipboard(transaction.correlationId, 'Correlation ID')}
                className="hover:text-electric-400 transition-colors"
                title="Copy Correlation ID"
              >
                <Copy className="w-3.5 h-3.5" />
              </button>
              {transaction.requestId && (
                <>
                  <span className="text-slate-600">|</span>
                  <span>Request: {transaction.requestId}</span>
                </>
              )}
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => copyToClipboard(window.location.href, 'Trace URL')}
            leftIcon={<Share2 className="w-3.5 h-3.5" />}
          >
            Share Trace
          </Button>
        </div>
      </div>

      {/* Failure Callout Banner if FAILED */}
      {transaction.transactionStatus === 'FAILED' && (
        <div className="bg-rose-500/10 border border-rose-500/30 rounded-xl p-4 flex items-start gap-3">
          <AlertTriangle className="w-5 h-5 text-rose-400 flex-shrink-0 mt-0.5" />
          <div className="space-y-1">
            <h4 className="text-sm font-semibold text-rose-300">Transaction Failure Detected</h4>
            <p className="text-xs text-rose-200/90 font-mono">
              {transaction.failureReason || 'A downstream microservice rejected execution or timed out.'}
            </p>
          </div>
        </div>
      )}

      {/* 4 Overview KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="p-4">
          <span className="text-xs text-slate-400 font-medium">Total Duration</span>
          <div className="flex items-center justify-between mt-1">
            <span
              className={`text-xl font-bold font-mono ${
                totalDuration > 1000 ? 'text-rose-400' : totalDuration > 400 ? 'text-amber-400' : 'text-emerald-400'
              }`}
            >
              {totalDuration} ms
            </span>
            <Clock className="w-5 h-5 text-slate-500" />
          </div>
          <p className="text-[11px] text-slate-400 mt-1">
            Initiated at {new Date(transaction.startTime).toLocaleTimeString()}
          </p>
        </Card>

        <Card className="p-4">
          <span className="text-xs text-slate-400 font-medium">Step Count</span>
          <div className="flex items-center justify-between mt-1">
            <span className="text-xl font-bold text-white">{steps.length} Hops</span>
            <Layers className="w-5 h-5 text-slate-500" />
          </div>
          <p className="text-[11px] text-slate-400 mt-1">
            {steps.filter((s) => s.status === 'SUCCESS').length} Successful,{' '}
            {steps.filter((s) => s.status === 'FAILED').length} Failed
          </p>
        </Card>

        <Card className="p-4">
          <span className="text-xs text-slate-400 font-medium">Root Ingress Service</span>
          <div className="flex items-center justify-between mt-1">
            <span className="text-base font-bold text-electric-400 font-mono truncate">
              {transaction.rootServiceName || 'api-gateway'}
            </span>
            <Server className="w-5 h-5 text-slate-500 flex-shrink-0" />
          </div>
          <p className="text-[11px] text-slate-400 mt-1">Edge router and authentication gateway</p>
        </Card>

        <Card className="p-4">
          <span className="text-xs text-slate-400 font-medium">Workflow Outcome</span>
          <div className="flex items-center justify-between mt-1">
            <span
              className={`text-xl font-bold ${
                transaction.transactionStatus === 'SUCCESS' ? 'text-emerald-400' : 'text-rose-400'
              }`}
            >
              {transaction.transactionStatus}
            </span>
            {transaction.transactionStatus === 'SUCCESS' ? (
              <CheckCircle2 className="w-5 h-5 text-emerald-400" />
            ) : (
              <XCircle className="w-5 h-5 text-rose-400" />
            )}
          </div>
          <p className="text-[11px] text-slate-400 mt-1">Distributed trace status</p>
        </Card>
      </div>

      {/* Distributed Execution Waterfall */}
      <Card
        title="Distributed Execution Waterfall"
        subtitle="Chronological service invocation waterfall with relative execution durations"
      >
        <div className="space-y-4 pt-2">
          {steps.length === 0 ? (
            <div className="p-8 text-center text-xs text-slate-400">
              No service steps recorded for this transaction.
            </div>
          ) : (
            <div className="space-y-3">
              {steps.map((step) => {
                const isExpanded = expandedStepId === step.id;
                const percentage = Math.max(4, Math.round((step.durationMs / totalDuration) * 100));

                return (
                  <div
                    key={step.id}
                    className="border border-navy-750/70 rounded-xl bg-navy-950/40 hover:bg-navy-950/70 transition-all p-3.5 space-y-2.5"
                  >
                    <div className="flex items-center justify-between gap-3">
                      <div className="flex items-center gap-2.5">
                        <button
                          onClick={() => setExpandedStepId(isExpanded ? null : step.id)}
                          className="p-1 hover:bg-navy-800 rounded text-slate-400 hover:text-white transition-colors"
                        >
                          {isExpanded ? <ChevronDown className="w-4 h-4" /> : <ChevronRight className="w-4 h-4" />}
                        </button>
                        <span className="w-6 h-6 rounded-full bg-navy-800 border border-navy-700 flex items-center justify-center text-[11px] font-mono font-bold text-slate-300">
                          {step.stepOrder}
                        </span>
                        <div>
                          <div className="flex items-center gap-2">
                            <span className="font-semibold text-xs text-white">{step.stepName}</span>
                            <span className="px-1.5 py-0.2 rounded bg-navy-800 border border-navy-700 text-[10px] font-mono text-slate-400">
                              {step.serviceName}
                            </span>
                          </div>
                        </div>
                      </div>

                      <div className="flex items-center gap-3">
                        <span className="font-mono text-xs font-semibold text-slate-300">
                          {step.durationMs} ms
                        </span>
                        <span
                          className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                            step.status === 'SUCCESS'
                              ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                              : step.status === 'FAILED'
                              ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                              : 'bg-slate-500/10 text-slate-400 border border-slate-500/20'
                          }`}
                        >
                          {step.status}
                        </span>
                      </div>
                    </div>

                    {/* Proportional Waterfall Bar */}
                    <div className="w-full bg-navy-900 rounded-full h-2 overflow-hidden flex">
                      <div
                        className={`h-full rounded-full transition-all duration-500 ${
                          step.status === 'SUCCESS'
                            ? 'bg-emerald-500'
                            : step.status === 'FAILED'
                            ? 'bg-rose-500'
                            : 'bg-slate-600'
                        }`}
                        style={{ width: `${percentage}%` }}
                      />
                    </div>

                    {/* Expandable Diagnostics & Snapshot */}
                    {isExpanded && (
                      <div className="pt-2 border-t border-navy-800 text-xs space-y-2">
                        {step.errorMessage && (
                          <div className="p-2.5 rounded bg-rose-500/10 border border-rose-500/20 text-rose-300 font-mono text-[11px]">
                            <strong>Error:</strong> {step.errorMessage}
                          </div>
                        )}
                        {step.payloadSnapshot && (
                          <div>
                            <span className="text-[11px] font-medium text-slate-400 block mb-1">
                              Step Context Snapshot:
                            </span>
                            <pre className="p-2.5 rounded bg-navy-950 border border-navy-800 font-mono text-[11px] text-slate-300 overflow-x-auto">
                              {step.payloadSnapshot}
                            </pre>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </Card>

      {/* Metadata JSON */}
      {transaction.metadata && (
        <Card title="Span Context Metadata" subtitle="Distributed trace tags and environment parameters">
          <pre className="p-3 bg-navy-950 rounded-lg border border-navy-800 font-mono text-xs text-slate-300 overflow-x-auto">
            {transaction.metadata}
          </pre>
        </Card>
      )}
    </div>
  );
};
