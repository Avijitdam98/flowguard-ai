import React, { useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  ArrowLeft,
  Sparkles,
  MessageSquare,
  Clock,
  ShieldAlert,
  CheckCircle2,
  User,
  RefreshCw,
  Send,
  AlertTriangle,
  History,
  Check,
  Bot,
  Shield,
} from 'lucide-react';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { LoadingSkeleton } from '../../components/ui/LoadingSkeleton';
import { EmptyState } from '../../components/ui/EmptyState';
import { Modal } from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import { incidentService } from '../../services/incidentService';
import { aiService } from '../../services/aiService';
import { IncidentSeverity, IncidentStatus } from '../../types/incident';
import { AiRootCauseResponse } from '../../types/ai';

export const IncidentDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const toast = useToast();

  const [commentText, setCommentText] = useState('');
  const [isAiModalOpen, setIsAiModalOpen] = useState(false);
  const [resolutionNotes, setResolutionNotes] = useState('');
  const [isResolveModalOpen, setIsResolveModalOpen] = useState(false);

  const lifecycleStages: IncidentStatus[] = ['DETECTED', 'ACKNOWLEDGED', 'INVESTIGATING', 'RESOLVED', 'CLOSED'];

  // Query Incident Details
  const {
    data: incident,
    isLoading,
    refetch,
  } = useQuery({
    queryKey: ['incident', id],
    queryFn: () => incidentService.getIncidentById(id!),
    enabled: !!id,
    refetchInterval: 10000,
  });

  // Query Comments
  const { data: comments = [] } = useQuery({
    queryKey: ['incidentComments', id],
    queryFn: () => incidentService.getComments(id!),
    enabled: !!id,
    refetchInterval: 10000,
  });

  // Query Timeline Events
  const { data: timeline = [] } = useQuery({
    queryKey: ['incidentTimeline', id],
    queryFn: () => incidentService.getTimeline(id!),
    enabled: !!id,
    refetchInterval: 10000,
  });

  // Query AI RCA Diagnosis dynamically from Ollama / AI engine
  const {
    data: aiRca,
    isLoading: isAiLoading,
    isError: isAiError,
    refetch: refetchAiRca,
  } = useQuery({
    queryKey: ['incidentRca', id],
    queryFn: () => aiService.getIncidentRca(id!),
    enabled: isAiModalOpen && !!id,
    staleTime: 60000,
  });

  // Update Status Mutation
  const updateStatusMutation = useMutation({
    mutationFn: (newStatus: IncidentStatus) =>
      incidentService.updateIncident(id!, { status: newStatus }),
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ['incident', id] });
      queryClient.invalidateQueries({ queryKey: ['incidentTimeline', id] });
      queryClient.invalidateQueries({ queryKey: ['incidents'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      toast.success('Status Updated', `Incident moved to ${updated.status}`);
    },
    onError: () => toast.error('Update Failed', 'Unable to transition status'),
  });

  // Resolve Mutation with notes
  const resolveMutation = useMutation({
    mutationFn: () =>
      incidentService.updateIncident(id!, {
        status: 'RESOLVED',
        resolutionNotes,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['incident', id] });
      queryClient.invalidateQueries({ queryKey: ['incidentTimeline', id] });
      queryClient.invalidateQueries({ queryKey: ['incidents'] });
      toast.success('Incident Resolved', 'Resolution notes recorded');
      setIsResolveModalOpen(false);
    },
    onError: () => toast.error('Error', 'Unable to resolve incident'),
  });

  // Add Comment Mutation
  const addCommentMutation = useMutation({
    mutationFn: (text: string) => incidentService.addComment(id!, text),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['incidentComments', id] });
      queryClient.invalidateQueries({ queryKey: ['incidentTimeline', id] });
      setCommentText('');
      toast.info('Note Added', 'Investigation comment appended');
    },
    onError: () => toast.error('Error', 'Unable to post comment'),
  });

  const handleCommentSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!commentText.trim()) return;
    addCommentMutation.mutate(commentText);
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <LoadingSkeleton className="h-16" />
        <LoadingSkeleton className="h-20" />
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

  if (!incident) {
    return (
      <EmptyState
        title="Incident Not Found"
        description="The requested incident could not be located."
        action={
          <Button variant="primary" size="sm" onClick={() => navigate('/incidents')}>
            Back to Incidents
          </Button>
        }
      />
    );
  }

  const getSeverityBadgeClass = (sev: IncidentSeverity) => {
    switch (sev) {
      case 'CRITICAL':
        return 'bg-rose-500/15 text-rose-300 border-rose-500/40 animate-pulse';
      case 'HIGH':
        return 'bg-amber-500/15 text-amber-300 border-amber-500/40';
      case 'MEDIUM':
        return 'bg-blue-500/15 text-blue-300 border-blue-500/40';
      case 'LOW':
        return 'bg-slate-500/15 text-slate-300 border-slate-500/40';
    }
  };

  const getStatusBadgeType = (status: IncidentStatus) => {
    switch (status) {
      case 'RESOLVED':
      case 'CLOSED':
        return 'HEALTHY';
      case 'INVESTIGATING':
        return 'DEGRADED';
      case 'DETECTED':
      case 'ACKNOWLEDGED':
      default:
        return 'DOWN';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <Link to="/incidents">
            <Button variant="secondary" size="sm" leftIcon={<ArrowLeft className="w-3.5 h-3.5" />}>
              Back
            </Button>
          </Link>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-mono text-base font-bold text-rose-400">{incident.incidentNumber}</span>
              <h1 className="text-xl font-bold tracking-tight text-white">{incident.title}</h1>
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              Assigned Lead: <span className="text-slate-200 font-semibold">{incident.assignedEngineerName || 'Unassigned'}</span>
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <span className={`px-2.5 py-1 rounded-full text-xs font-bold border ${getSeverityBadgeClass(incident.severity)}`}>
            {incident.severity}
          </span>
          <StatusBadge status={getStatusBadgeType(incident.status)} pulse={incident.status === 'INVESTIGATING'} />
          <Button
            variant="primary"
            size="sm"
            onClick={() => setIsAiModalOpen(true)}
            leftIcon={<Sparkles className="w-4 h-4 text-electric-300" />}
          >
            AI Root Cause Analysis
          </Button>
        </div>
      </div>

      {/* Lifecycle Progress Pipeline */}
      <Card className="p-4">
        <div className="text-xs font-semibold text-slate-400 mb-3 uppercase tracking-wider">
          Incident Lifecycle Progression
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-5 gap-2">
          {lifecycleStages.map((stage, idx) => {
            const currentIdx = lifecycleStages.indexOf(incident.status);
            const isCompleted = currentIdx >= idx;
            const isCurrent = incident.status === stage;

            return (
              <button
                key={stage}
                onClick={() => {
                  if (stage === 'RESOLVED') {
                    setIsResolveModalOpen(true);
                  } else {
                    updateStatusMutation.mutate(stage);
                  }
                }}
                disabled={updateStatusMutation.isPending}
                className={`p-2.5 rounded-lg border text-left transition-all ${
                  isCurrent
                    ? 'bg-electric-500/20 border-electric-500 text-white shadow-lg shadow-electric-500/10'
                    : isCompleted
                    ? 'bg-navy-950 border-emerald-500/40 text-emerald-300'
                    : 'bg-navy-950/40 border-navy-800 text-slate-500 hover:border-slate-700'
                }`}
              >
                <div className="flex items-center justify-between text-[10px] font-bold">
                  <span>STEP {idx + 1}</span>
                  {isCompleted && !isCurrent ? (
                    <Check className="w-3 h-3 text-emerald-400" />
                  ) : isCurrent ? (
                    <div className="w-2 h-2 rounded-full bg-electric-400 animate-pulse" />
                  ) : null}
                </div>
                <div className="font-semibold text-xs mt-1 truncate">{stage}</div>
              </button>
            );
          })}
        </div>
      </Card>

      {/* 4 Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="p-4">
          <span className="text-xs text-slate-400 font-medium">Incident Severity</span>
          <div className="flex items-center justify-between mt-1">
            <span
              className={`text-xl font-bold ${
                incident.severity === 'CRITICAL' ? 'text-rose-400' : 'text-amber-400'
              }`}
            >
              {incident.severity}
            </span>
            <ShieldAlert className="w-5 h-5 text-slate-500" />
          </div>
          <p className="text-[11px] text-slate-400 mt-1">Impacts core user flows</p>
        </Card>

        <Card className="p-4">
          <span className="text-xs text-slate-400 font-medium">Current Status</span>
          <div className="flex items-center justify-between mt-1">
            <span className="text-xl font-bold text-electric-400">{incident.status}</span>
            <Clock className="w-5 h-5 text-slate-500" />
          </div>
          <p className="text-[11px] text-slate-400 mt-1">
            Reported {new Date(incident.createdAt).toLocaleTimeString()}
          </p>
        </Card>

        <Card className="p-4">
          <span className="text-xs text-slate-400 font-medium">Assigned Engineer</span>
          <div className="flex items-center justify-between mt-1">
            <span className="text-base font-bold text-white truncate">
              {incident.assignedEngineerName || 'Unassigned'}
            </span>
            <User className="w-5 h-5 text-slate-500" />
          </div>
          <p className="text-[11px] text-slate-400 mt-1">Incident commander</p>
        </Card>

        <Card className="p-4">
          <span className="text-xs text-slate-400 font-medium">Resolution Time</span>
          <div className="flex items-center justify-between mt-1">
            <span className="text-xl font-bold text-emerald-400">
              {incident.resolvedAt ? 'Resolved' : 'Active'}
            </span>
            <CheckCircle2 className="w-5 h-5 text-emerald-500/60" />
          </div>
          <p className="text-[11px] text-slate-400 mt-1">
            {incident.resolvedAt ? new Date(incident.resolvedAt).toLocaleString() : 'Investigation in progress'}
          </p>
        </Card>
      </div>

      {/* Description & Resolution Notes */}
      <Card title="Incident Details &amp; Observed Impact">
        <div className="space-y-4 text-xs">
          <p className="text-slate-200 leading-relaxed">{incident.description}</p>

          {incident.resolutionNotes && (
            <div className="p-3 bg-emerald-500/10 border border-emerald-500/20 rounded-lg text-emerald-300">
              <strong className="block font-semibold mb-1">Resolution Post-Mortem Notes:</strong>
              {incident.resolutionNotes}
            </div>
          )}
        </div>
      </Card>

      {/* 2-Column Layout: Comments & Investigation Log vs Audit Timeline */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left: Investigation Notes Thread */}
        <Card
          title="Investigation Notes &amp; Findings"
          subtitle="Collaborative discussion thread for on-call engineers"
        >
          <div className="space-y-4 pt-2">
            <div className="space-y-3 max-h-80 overflow-y-auto pr-1">
              {comments.length === 0 ? (
                <div className="p-6 text-center text-xs text-slate-500">
                  No notes posted yet. Add observation details or hypotheses below.
                </div>
              ) : (
                comments.map((c) => (
                  <div key={c.id} className="p-3 rounded-lg bg-navy-950 border border-navy-800 space-y-1">
                    <div className="flex items-center justify-between text-[11px]">
                      <span className="font-semibold text-electric-400">{c.userName || 'Engineer'}</span>
                      <span className="text-slate-500">{new Date(c.createdAt).toLocaleTimeString()}</span>
                    </div>
                    <p className="text-xs text-slate-200">{c.commentText}</p>
                  </div>
                ))
              )}
            </div>

            <form onSubmit={handleCommentSubmit} className="flex gap-2 pt-2 border-t border-navy-750">
              <input
                type="text"
                placeholder="Post a diagnostic finding or log note..."
                value={commentText}
                onChange={(e) => setCommentText(e.target.value)}
                className="flex-1 bg-navy-950 border border-navy-750 text-xs rounded-lg px-3 py-2 text-slate-200 focus:outline-none focus:border-electric-500"
              />
              <Button
                variant="primary"
                size="sm"
                type="submit"
                isLoading={addCommentMutation.isPending}
                leftIcon={<Send className="w-3.5 h-3.5" />}
              >
                Post
              </Button>
            </form>
          </div>
        </Card>

        {/* Right: Chronological Audit Trail Timeline */}
        <Card
          title="Audit Trail Timeline"
          subtitle="Immutable event log of detections, state changes, and engineer actions"
        >
          <div className="space-y-3 max-h-96 overflow-y-auto pt-2 pr-1">
            {timeline.length === 0 ? (
              <div className="p-6 text-center text-xs text-slate-500">No events logged yet.</div>
            ) : (
              timeline.map((evt) => (
                <div key={evt.id} className="flex items-start gap-3 p-2.5 rounded-lg bg-navy-950/40 border border-navy-800 text-xs">
                  <div className="w-2 h-2 rounded-full bg-electric-400 flex-shrink-0 mt-1.5" />
                  <div className="flex-1 space-y-0.5">
                    <div className="flex items-center justify-between text-[11px]">
                      <span className="font-semibold text-slate-300 font-mono">{evt.eventType}</span>
                      <span className="text-slate-500">{new Date(evt.createdAt).toLocaleTimeString()}</span>
                    </div>
                    <p className="text-slate-400 text-[11px]">{evt.eventDescription}</p>
                  </div>
                </div>
              ))
            )}
          </div>
        </Card>
      </div>

      {/* AI Root Cause Analysis Modal */}
      <Modal
        isOpen={isAiModalOpen}
        onClose={() => setIsAiModalOpen(false)}
        title="AI-Powered Incident Diagnosis & Root Cause Analysis"
        subtitle={`Analyzing telemetry signals for ${incident.incidentNumber}`}
        maxWidth="xl"
      >
        <div className="space-y-4 text-xs">
          {isAiLoading ? (
            <div className="space-y-3 py-6 text-center">
              <RefreshCw className="w-8 h-8 text-electric-400 animate-spin mx-auto mb-2" />
              <p className="text-sm font-semibold text-white">Synthesizing Telemetry Signals...</p>
              <p className="text-slate-400 text-xs max-w-sm mx-auto">
                Consulting Ollama LLM with correlation traces, error logs, and probe data...
              </p>
            </div>
          ) : isAiError ? (
            <div className="p-4 bg-rose-500/10 border border-rose-500/30 rounded-lg text-rose-300 text-center space-y-2">
              <AlertTriangle className="w-6 h-6 text-rose-400 mx-auto" />
              <p className="font-semibold">AI Analysis Query Failed</p>
              <p className="text-slate-400 text-[11px]">Could not contact the FlowGuard AI backend service.</p>
              <Button size="sm" variant="secondary" onClick={() => refetchAiRca()} className="mt-2">
                Retry Diagnosis
              </Button>
            </div>
          ) : aiRca ? (
            <>
              <div className="bg-electric-500/10 border border-electric-500/20 rounded-lg p-3 flex items-start gap-2.5 text-electric-300">
                <Bot className="w-5 h-5 text-electric-400 flex-shrink-0 mt-0.5" />
                <div className="space-y-1">
                  <div className="flex items-center justify-between">
                    <strong>AI Telemetry Synthesis:</strong>
                    <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-navy-950 border border-electric-500/30 text-electric-300">
                      Model: {aiRca.modelUsed || 'Ollama (qwen2.5:7b)'}
                    </span>
                  </div>
                  <p className="text-slate-200 text-xs leading-relaxed">{aiRca.summary}</p>
                </div>
              </div>

              {aiRca.potentialRootCauses && aiRca.potentialRootCauses.length > 0 && (
                <div className="p-3 bg-navy-950 rounded-lg border border-navy-800 space-y-1.5">
                  <span className="text-slate-400 font-semibold block text-[11px] uppercase tracking-wider">
                    Potential Root Causes Identified ({Math.round((aiRca.confidenceScore || 0) * 100)}% Confidence)
                  </span>
                  <ul className="list-disc list-inside space-y-1 text-slate-200">
                    {aiRca.potentialRootCauses.map((cause, i) => (
                      <li key={i}>{cause}</li>
                    ))}
                  </ul>
                </div>
              )}

              {aiRca.recommendedInvestigationSteps && aiRca.recommendedInvestigationSteps.length > 0 && (
                <div className="p-3 bg-navy-950 rounded-lg border border-navy-800 space-y-1.5">
                  <span className="text-slate-400 font-semibold block text-[11px] uppercase tracking-wider">
                    Recommended Investigation Steps
                  </span>
                  <ul className="list-decimal list-inside space-y-1 text-slate-300">
                    {aiRca.recommendedInvestigationSteps.map((step, i) => (
                      <li key={i}>{step}</li>
                    ))}
                  </ul>
                </div>
              )}

              {aiRca.suggestedRemediationActions && aiRca.suggestedRemediationActions.length > 0 && (
                <div className="p-3 bg-navy-950 rounded-lg border border-navy-800 space-y-1.5">
                  <span className="text-slate-400 font-semibold block text-[11px] uppercase tracking-wider">
                    Suggested Remediation Actions
                  </span>
                  <ul className="list-disc list-inside space-y-1 text-slate-300">
                    {aiRca.suggestedRemediationActions.map((action, i) => (
                      <li key={i}>{action}</li>
                    ))}
                  </ul>
                </div>
              )}

              <div className="bg-amber-500/10 border border-amber-500/20 rounded-lg p-2.5 flex items-start gap-2 text-amber-300 text-[11px]">
                <Shield className="w-4 h-4 text-amber-400 flex-shrink-0 mt-0.5" />
                <div>
                  <strong>Safety Notice:</strong> {aiRca.disclaimer || 'AI recommendations are advisory and must be verified before executing changes.'}
                </div>
              </div>
            </>
          ) : null}

          <div className="flex justify-end pt-2 border-t border-navy-750">
            <Button variant="ghost" onClick={() => setIsAiModalOpen(false)}>
              Close Analysis
            </Button>
          </div>
        </div>
      </Modal>

      {/* Resolve Incident Modal */}
      <Modal
        isOpen={isResolveModalOpen}
        onClose={() => setIsResolveModalOpen(false)}
        title="Resolve Incident"
        subtitle={`Document post-mortem notes for ${incident.incidentNumber}`}
        maxWidth="md"
      >
        <div className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">
              Resolution Summary &amp; Corrective Action Taken *
            </label>
            <textarea
              rows={4}
              placeholder="Explain how the incident was mitigated (e.g. rolled back deployment, scaled pod replicas, flushed queue)..."
              value={resolutionNotes}
              onChange={(e) => setResolutionNotes(e.target.value)}
              className="w-full bg-navy-950 border border-navy-750 text-xs rounded-lg p-3 text-slate-200 focus:outline-none focus:border-electric-500"
              required
            />
          </div>

          <div className="flex justify-end gap-2 pt-2 border-t border-navy-750">
            <Button variant="ghost" onClick={() => setIsResolveModalOpen(false)}>
              Cancel
            </Button>
            <Button
              variant="primary"
              isLoading={resolveMutation.isPending}
              onClick={() => resolveMutation.mutate()}
            >
              Mark as Resolved
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
