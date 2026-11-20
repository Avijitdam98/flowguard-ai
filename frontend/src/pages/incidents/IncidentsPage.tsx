import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import {
  AlertOctagon,
  Plus,
  Search,
  ExternalLink,
  RefreshCw,
  Flame,
  Clock,
  CheckCircle2,
  AlertTriangle,
  User,
  ShieldAlert,
} from 'lucide-react';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { LoadingSkeleton } from '../../components/ui/LoadingSkeleton';
import { EmptyState } from '../../components/ui/EmptyState';
import { Modal } from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import { incidentService } from '../../services/incidentService';
import {
  CreateIncidentRequest,
  Incident,
  IncidentSeverity,
  IncidentStatus,
} from '../../types/incident';

export const IncidentsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const toast = useToast();

  const [search, setSearch] = useState('');
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');
  const [selectedSeverity, setSelectedSeverity] = useState<string>('ALL');
  const [isDeclareModalOpen, setIsDeclareModalOpen] = useState(false);
  const [page, setPage] = useState(0);

  // Form State for Declare Incident
  const [formData, setFormData] = useState<CreateIncidentRequest>({
    title: '',
    description: '',
    severity: 'HIGH',
    assignedEngineerName: 'Sarah Chen',
  });

  // Query incidents
  const {
    data: paginatedData,
    isLoading,
    isFetching,
    refetch,
  } = useQuery({
    queryKey: ['incidents', selectedStatus, selectedSeverity, search, page],
    queryFn: () =>
      incidentService.getAllIncidents({
        status: selectedStatus !== 'ALL' ? (selectedStatus as IncidentStatus) : undefined,
        severity: selectedSeverity !== 'ALL' ? (selectedSeverity as IncidentSeverity) : undefined,
        search: search.trim() || undefined,
        page,
        size: 15,
      }),
    refetchInterval: 15000,
  });

  // Declare Incident Mutation
  const declareMutation = useMutation({
    mutationFn: (req: CreateIncidentRequest) => incidentService.createIncident(req),
    onSuccess: (created) => {
      queryClient.invalidateQueries({ queryKey: ['incidents'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      toast.warning('Incident Declared', `${created.incidentNumber} has been logged with ${created.severity} severity`);
      setIsDeclareModalOpen(false);
      setFormData({
        title: '',
        description: '',
        severity: 'HIGH',
        assignedEngineerName: 'Sarah Chen',
      });
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to declare incident';
      toast.error('Error', msg);
    },
  });

  const handleDeclareSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.title.trim() || !formData.description.trim()) {
      toast.warning('Required Fields', 'Please provide a title and incident description');
      return;
    }
    declareMutation.mutate(formData);
  };

  const incidents = paginatedData?.content || [];
  const totalElements = paginatedData?.totalElements || 0;
  const totalPages = paginatedData?.totalPages || 0;

  const openIncidentsCount = incidents.filter(
    (i) => i.status !== 'RESOLVED' && i.status !== 'CLOSED'
  ).length;
  const criticalCount = incidents.filter((i) => i.severity === 'CRITICAL').length;

  const getSeverityBadge = (sev: IncidentSeverity) => {
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
        <div>
          <h1 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
            <AlertOctagon className="w-5 h-5 text-rose-500" /> Incident Management Command Center
            {isFetching && <RefreshCw className="w-3.5 h-3.5 text-slate-400 animate-spin" />}
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Automated anomaly triage, multi-engineer investigation collaboration, and AI root cause analysis
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
            variant="danger"
            size="sm"
            onClick={() => setIsDeclareModalOpen(true)}
            leftIcon={<Flame className="w-4 h-4" />}
          >
            Declare Incident
          </Button>
        </div>
      </div>

      {/* KPI Stats Bar */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <div className="p-3 bg-navy-900/60 border border-navy-750 rounded-xl flex items-center justify-between">
          <div>
            <span className="text-[11px] text-slate-400">Active Incidents</span>
            <p className="text-xl font-bold text-white mt-0.5">{openIncidentsCount}</p>
          </div>
          <Flame className="w-6 h-6 text-rose-400 opacity-80" />
        </div>

        <div className="p-3 bg-navy-900/60 border border-navy-750 rounded-xl flex items-center justify-between">
          <div>
            <span className="text-[11px] text-rose-400">Critical Outages</span>
            <p className="text-xl font-bold text-rose-400 mt-0.5">{criticalCount}</p>
          </div>
          <ShieldAlert className="w-6 h-6 text-rose-500 opacity-80" />
        </div>

        <div className="p-3 bg-navy-900/60 border border-navy-750 rounded-xl flex items-center justify-between">
          <div>
            <span className="text-[11px] text-emerald-400">Mean Time to Resolve (MTTR)</span>
            <p className="text-xl font-bold text-emerald-400 mt-0.5">18.4 mins</p>
          </div>
          <Clock className="w-6 h-6 text-emerald-500 opacity-80" />
        </div>
      </div>

      {/* Filter and Search Bar */}
      <Card className="p-3">
        <div className="flex flex-col md:flex-row gap-3 items-center justify-between">
          <div className="relative flex-1 w-full">
            <Search className="absolute left-3 top-2.5 w-4 h-4 text-slate-400" />
            <input
              type="text"
              placeholder="Search incidents by reference #, title, or description..."
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
              value={selectedSeverity}
              onChange={(e) => {
                setSelectedSeverity(e.target.value);
                setPage(0);
              }}
              className="bg-navy-950/60 border border-navy-750 text-xs rounded-lg px-2.5 py-2 text-slate-300 focus:outline-none focus:border-electric-500"
            >
              <option value="ALL">All Severities</option>
              <option value="CRITICAL">Critical</option>
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
            </select>

            <select
              value={selectedStatus}
              onChange={(e) => {
                setSelectedStatus(e.target.value);
                setPage(0);
              }}
              className="bg-navy-950/60 border border-navy-750 text-xs rounded-lg px-2.5 py-2 text-slate-300 focus:outline-none focus:border-electric-500"
            >
              <option value="ALL">All Statuses</option>
              <option value="DETECTED">Detected</option>
              <option value="ACKNOWLEDGED">Acknowledged</option>
              <option value="INVESTIGATING">Investigating</option>
              <option value="RESOLVED">Resolved</option>
              <option value="CLOSED">Closed</option>
            </select>
          </div>
        </div>
      </Card>

      {/* Incidents Table */}
      <Card>
        {isLoading ? (
          <div className="p-4 space-y-3">
            <LoadingSkeleton count={8} height="h-10" />
          </div>
        ) : incidents.length === 0 ? (
          <EmptyState
            title="No Incidents Reported"
            description={
              search || selectedStatus !== 'ALL' || selectedSeverity !== 'ALL'
                ? 'Try adjusting your filters or search keywords.'
                : 'All microservices and monitoring checks are operating within normal parameters.'
            }
            icon={<CheckCircle2 className="w-8 h-8 text-emerald-400" />}
          />
        ) : (
          <div>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-navy-750 text-[11px] font-semibold uppercase text-slate-400">
                    <th className="px-4 py-3">Incident #</th>
                    <th className="px-4 py-3">Title & Summary</th>
                    <th className="px-4 py-3">Severity</th>
                    <th className="px-4 py-3">Status</th>
                    <th className="px-4 py-3">Assigned Engineer</th>
                    <th className="px-4 py-3">Reported</th>
                    <th className="px-4 py-3 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-navy-750/50 text-slate-200">
                  {incidents.map((inc) => (
                    <tr key={inc.id} className="hover:bg-navy-800/40 transition-colors group">
                      <td className="px-4 py-3 font-mono font-bold text-white whitespace-nowrap">
                        <Link
                          to={`/incidents/${inc.id}`}
                          className="hover:text-electric-400 flex items-center gap-1.5 transition-colors"
                        >
                          {inc.incidentNumber}
                          <ExternalLink className="w-3 h-3 opacity-0 group-hover:opacity-100 transition-opacity text-slate-400" />
                        </Link>
                      </td>

                      <td className="px-4 py-3">
                        <div className="font-semibold text-white truncate max-w-sm">{inc.title}</div>
                        <div className="text-[11px] text-slate-400 truncate max-w-sm mt-0.5">{inc.description}</div>
                      </td>

                      <td className="px-4 py-3">
                        <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${getSeverityBadge(inc.severity)}`}>
                          {inc.severity}
                        </span>
                      </td>

                      <td className="px-4 py-3">
                        <StatusBadge status={getStatusBadgeType(inc.status)} />
                      </td>

                      <td className="px-4 py-3 text-slate-300">
                        <div className="flex items-center gap-1.5">
                          <User className="w-3.5 h-3.5 text-slate-400" />
                          <span>{inc.assignedEngineerName || 'Unassigned'}</span>
                        </div>
                      </td>

                      <td className="px-4 py-3 text-slate-400 whitespace-nowrap text-[11px]">
                        {new Date(inc.createdAt).toLocaleString([], {
                          month: 'short',
                          day: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </td>

                      <td className="px-4 py-3 text-right">
                        <Link to={`/incidents/${inc.id}`}>
                          <Button variant="ghost" size="sm">
                            Investigate
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
                  Showing {page * 15 + 1} to {Math.min((page + 1) * 15, totalElements)} of {totalElements} incidents
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

      {/* Declare Incident Modal */}
      <Modal
        isOpen={isDeclareModalOpen}
        onClose={() => setIsDeclareModalOpen(false)}
        title="Declare Engineering Incident"
        subtitle="Escalate an active service disruption and mobilize on-call engineering response"
        maxWidth="lg"
      >
        <form onSubmit={handleDeclareSubmit} className="space-y-4">
          <Input
            label="Incident Title *"
            placeholder="e.g. Elevated 504 Timeouts in Payment Gateway"
            value={formData.title}
            onChange={(e) => setFormData({ ...formData, title: e.target.value })}
            required
          />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Severity *</label>
              <select
                value={formData.severity}
                onChange={(e) => setFormData({ ...formData, severity: e.target.value as IncidentSeverity })}
                className="w-full bg-navy-950/60 border border-navy-750 text-xs rounded-lg px-3 py-2 text-slate-200 focus:outline-none focus:border-electric-500"
              >
                <option value="CRITICAL">CRITICAL – Outage affecting core transactions</option>
                <option value="HIGH">HIGH – Severe degradation without full outage</option>
                <option value="MEDIUM">MEDIUM – Degraded non-critical workflow</option>
                <option value="LOW">LOW – Minor defect or telemetry glitch</option>
              </select>
            </div>

            <Input
              label="Assigned Lead Engineer"
              placeholder="e.g. Sarah Chen"
              value={formData.assignedEngineerName || ''}
              onChange={(e) => setFormData({ ...formData, assignedEngineerName: e.target.value })}
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">Description & Initial Symptoms *</label>
            <textarea
              rows={4}
              placeholder="Describe observed errors, metrics spikes, affected customer cohorts, and initial hypotheses..."
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="w-full bg-navy-950/60 border border-navy-750 text-xs rounded-lg p-3 text-slate-200 focus:outline-none focus:border-electric-500"
              required
            />
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-navy-750">
            <Button variant="ghost" type="button" onClick={() => setIsDeclareModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="danger" type="submit" isLoading={declareMutation.isPending}>
              Declare Incident
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
