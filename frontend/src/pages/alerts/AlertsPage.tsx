import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  BellRing,
  Plus,
  RefreshCw,
  Trash2,
  CheckCircle2,
  AlertTriangle,
  History,
  Sliders,
  Check,
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
import { alertService } from '../../services/alertService';
import {
  AlertHistory,
  AlertRule,
  ComparisonOperator,
  CreateAlertRuleRequest,
  MetricType,
  TargetType,
} from '../../types/alert';
import { IncidentSeverity } from '../../types/incident';

export const AlertsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const toast = useToast();

  const [activeTab, setActiveTab] = useState<'rules' | 'history'>('rules');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [ruleToDelete, setRuleToDelete] = useState<AlertRule | null>(null);

  // Form State
  const [formData, setFormData] = useState<CreateAlertRuleRequest>({
    name: '',
    description: '',
    targetType: 'API_MONITOR',
    metricType: 'RESPONSE_TIME',
    comparisonOperator: 'GREATER_THAN',
    thresholdValue: 1500,
    evaluationWindowMinutes: 5,
    severity: 'HIGH',
    isEnabled: true,
  });

  // Query Alert Rules
  const {
    data: rulesData,
    isLoading: isRulesLoading,
    refetch: refetchRules,
  } = useQuery({
    queryKey: ['alertRules'],
    queryFn: () => alertService.getAllRules({ size: 50 }),
  });

  // Query Alert History
  const {
    data: historyData,
    isLoading: isHistoryLoading,
    refetch: refetchHistory,
  } = useQuery({
    queryKey: ['alertHistory'],
    queryFn: () => alertService.getAlertHistory({ size: 50 }),
    refetchInterval: 15000,
  });

  // Create Rule Mutation
  const createRuleMutation = useMutation({
    mutationFn: (req: CreateAlertRuleRequest) => alertService.createRule(req),
    onSuccess: (created) => {
      queryClient.invalidateQueries({ queryKey: ['alertRules'] });
      toast.success('Alert Rule Created', `"${created.name}" is now monitoring thresholds`);
      setIsCreateModalOpen(false);
      setFormData({
        name: '',
        description: '',
        targetType: 'API_MONITOR',
        metricType: 'RESPONSE_TIME',
        comparisonOperator: 'GREATER_THAN',
        thresholdValue: 1500,
        evaluationWindowMinutes: 5,
        severity: 'HIGH',
        isEnabled: true,
      });
    },
    onError: () => toast.error('Error', 'Unable to create alert rule'),
  });

  // Toggle Rule Mutation
  const toggleMutation = useMutation({
    mutationFn: (id: string) => alertService.toggleRule(id),
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ['alertRules'] });
      toast.info('Rule Updated', `${updated.name} is now ${updated.isEnabled ? 'Enabled' : 'Disabled'}`);
    },
    onError: () => toast.error('Error', 'Unable to toggle alert rule'),
  });

  // Delete Rule Mutation
  const deleteMutation = useMutation({
    mutationFn: (id: string) => alertService.deleteRule(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['alertRules'] });
      toast.success('Rule Deleted', 'Alert rule was removed');
      setRuleToDelete(null);
    },
    onError: () => toast.error('Error', 'Unable to delete alert rule'),
  });

  // Acknowledge Alert Mutation
  const acknowledgeMutation = useMutation({
    mutationFn: (id: string) => alertService.acknowledgeAlert(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['alertHistory'] });
      toast.success('Alert Acknowledged', 'Recorded engineer acknowledgement');
    },
    onError: () => toast.error('Error', 'Unable to acknowledge alert'),
  });

  const handleCreateSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name.trim()) {
      toast.warning('Required Field', 'Please enter an alert rule name');
      return;
    }
    createRuleMutation.mutate(formData);
  };

  const rules = rulesData?.content || [];
  const history = historyData?.content || [];
  const unacknowledgedCount = history.filter((h) => !h.isAcknowledged).length;

  const getSeverityBadgeClass = (sev: IncidentSeverity) => {
    switch (sev) {
      case 'CRITICAL':
        return 'bg-rose-500/15 text-rose-300 border-rose-500/40';
      case 'HIGH':
        return 'bg-amber-500/15 text-amber-300 border-amber-500/40';
      case 'MEDIUM':
        return 'bg-blue-500/15 text-blue-300 border-blue-500/40';
      case 'LOW':
        return 'bg-slate-500/15 text-slate-300 border-slate-500/40';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
            <BellRing className="w-5 h-5 text-electric-400" /> Alert Rules &amp; Notification Engine
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Define multi-signal threshold triggers, automated paging escalation, and event acknowledgement logs
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => {
              refetchRules();
              refetchHistory();
            }}
            leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
          >
            Refresh
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={() => setIsCreateModalOpen(true)}
            leftIcon={<Plus className="w-4 h-4" />}
          >
            Create Alert Rule
          </Button>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-navy-750 gap-4 text-xs font-semibold">
        <button
          onClick={() => setActiveTab('rules')}
          className={`pb-2.5 flex items-center gap-2 border-b-2 transition-colors ${activeTab === 'rules'
            ? 'border-electric-500 text-white'
            : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
        >
          <Sliders className="w-4 h-4" /> Configured Rules ({rules.length})
        </button>
        <button
          onClick={() => setActiveTab('history')}
          className={`pb-2.5 flex items-center gap-2 border-b-2 transition-colors ${activeTab === 'history'
            ? 'border-electric-500 text-white'
            : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
        >
          <History className="w-4 h-4" /> Triggered Events
          {unacknowledgedCount > 0 && (
            <span className="px-1.5 py-0.2 rounded-full text-[10px] font-bold bg-rose-500 text-white">
              {unacknowledgedCount}
            </span>
          )}
        </button>
      </div>

      {/* Tab 1: Rules Table */}
      {activeTab === 'rules' && (
        <Card>
          {isRulesLoading ? (
            <div className="p-4 space-y-3">
              <LoadingSkeleton count={6} height="h-10" />
            </div>
          ) : rules.length === 0 ? (
            <EmptyState
              title="No Alert Rules Configured"
              description="Establish proactive latency, error rate, or availability thresholds."
              action={
                <Button variant="primary" size="sm" onClick={() => setIsCreateModalOpen(true)}>
                  Create Rule
                </Button>
              }
            />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-navy-750 text-[11px] font-semibold uppercase text-slate-400">
                    <th className="px-4 py-3">Rule Name &amp; Description</th>
                    <th className="px-4 py-3">Target Scope</th>
                    <th className="px-4 py-3">Metric Condition</th>
                    <th className="px-4 py-3">Evaluation Window</th>
                    <th className="px-4 py-3">Severity</th>
                    <th className="px-4 py-3">Active</th>
                    <th className="px-4 py-3 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-navy-750/50 text-slate-200">
                  {rules.map((rule) => (
                    <tr key={rule.id} className="hover:bg-navy-800/40 transition-colors">
                      <td className="px-4 py-3 font-medium text-white">
                        <div className="font-semibold text-white">{rule.name}</div>
                        <div className="text-[11px] text-slate-400 max-w-sm truncate mt-0.5">
                          {rule.description || 'Threshold rule'}
                        </div>
                      </td>

                      <td className="px-4 py-3">
                        <span className="px-2 py-0.5 rounded bg-navy-800 border border-navy-750 text-[11px] font-mono">
                          {rule.targetType}
                        </span>
                      </td>

                      <td className="px-4 py-3 font-mono text-electric-400 font-semibold">
                        {rule.metricType} {rule.comparisonOperator === 'GREATER_THAN' ? '>' : '<'}{' '}
                        {rule.thresholdValue}
                        {rule.metricType === 'RESPONSE_TIME' ? 'ms' : '%'}
                      </td>

                      <td className="px-4 py-3 text-slate-300">
                        {rule.evaluationWindowMinutes} minutes
                      </td>

                      <td className="px-4 py-3">
                        <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${getSeverityBadgeClass(rule.severity)}`}>
                          {rule.severity}
                        </span>
                      </td>

                      <td className="px-4 py-3">
                        <button
                          onClick={() => toggleMutation.mutate(rule.id)}
                          className={`relative inline-flex h-5 w-9 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none ${rule.isEnabled ? 'bg-electric-500' : 'bg-slate-700'
                            }`}
                        >
                          <span
                            className={`pointer-events-none inline-block h-4 w-4 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out ${rule.isEnabled ? 'translate-x-4' : 'translate-x-0'
                              }`}
                          />
                        </button>
                      </td>

                      <td className="px-4 py-3 text-right">
                        <button
                          onClick={() => setRuleToDelete(rule)}
                          className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded transition-colors"
                          title="Delete Rule"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Card>
      )}

      {/* Tab 2: Triggered Alert History */}
      {activeTab === 'history' && (
        <Card>
          {isHistoryLoading ? (
            <div className="p-4 space-y-3">
              <LoadingSkeleton count={6} height="h-10" />
            </div>
          ) : history.length === 0 ? (
            <EmptyState
              title="No Triggered Alert Events"
              description="No threshold anomalies have been recorded recently."
              icon={<CheckCircle2 className="w-8 h-8 text-emerald-400" />}
            />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-navy-750 text-[11px] font-semibold uppercase text-slate-400">
                    <th className="px-4 py-3">Triggered Time</th>
                    <th className="px-4 py-3">Alert Rule</th>
                    <th className="px-4 py-3">Breached Metric Message</th>
                    <th className="px-4 py-3">Severity</th>
                    <th className="px-4 py-3">Acknowledgement</th>
                    <th className="px-4 py-3 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-navy-750/50 text-slate-200">
                  {history.map((h) => (
                    <tr key={h.id} className="hover:bg-navy-800/40 transition-colors">
                      <td className="px-4 py-3 text-slate-400 whitespace-nowrap text-[11px]">
                        {new Date(h.triggeredAt).toLocaleString([], {
                          month: 'short',
                          day: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </td>

                      <td className="px-4 py-3 font-semibold text-white truncate max-w-xs">
                        {h.alertRuleName || 'Threshold Alert'}
                      </td>

                      <td className="px-4 py-3 font-mono text-slate-200 max-w-sm">
                        {h.message}
                      </td>

                      <td className="px-4 py-3">
                        <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${getSeverityBadgeClass(h.severity)}`}>
                          {h.severity}
                        </span>
                      </td>

                      <td className="px-4 py-3">
                        {h.isAcknowledged ? (
                          <div className="text-[11px] text-emerald-400 flex items-center gap-1">
                            <Check className="w-3.5 h-3.5" />
                            <span>Acknowledged by {h.acknowledgedByName || 'Engineer'}</span>
                          </div>
                        ) : (
                          <span className="text-[11px] text-rose-400 font-semibold flex items-center gap-1">
                            <AlertTriangle className="w-3.5 h-3.5" /> Unacknowledged
                          </span>
                        )}
                      </td>

                      <td className="px-4 py-3 text-right">
                        {!h.isAcknowledged && (
                          <Button
                            variant="secondary"
                            size="sm"
                            onClick={() => acknowledgeMutation.mutate(h.id)}
                            isLoading={acknowledgeMutation.isPending}
                          >
                            Acknowledge
                          </Button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Card>
      )}

      {/* Create Alert Rule Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title="Configure Threshold Alert Rule"
        subtitle="Specify resource metric boundaries and automated severity tagging"
        maxWidth="lg"
      >
        <form onSubmit={handleCreateSubmit} className="space-y-4">
          <Input
            label="Rule Name *"
            placeholder="e.g. Gateway Latency Spike Alert"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            required
          />

          <Input
            label="Description"
            placeholder="e.g. Triggers when response latency exceeds SLA bounds"
            value={formData.description || ''}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
          />

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Target Scope</label>
              <select
                value={formData.targetType}
                onChange={(e) => setFormData({ ...formData, targetType: e.target.value as TargetType })}
                className="w-full bg-navy-950/60 border border-navy-750 text-xs rounded-lg px-3 py-2 text-slate-200 focus:outline-none focus:border-electric-500"
              >
                <option value="API_MONITOR">API Monitor</option>
                <option value="SERVICE">Microservice</option>
                <option value="GLOBAL">Global Architecture</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Metric Type</label>
              <select
                value={formData.metricType}
                onChange={(e) => setFormData({ ...formData, metricType: e.target.value as MetricType })}
                className="w-full bg-navy-950/60 border border-navy-750 text-xs rounded-lg px-3 py-2 text-slate-200 focus:outline-none focus:border-electric-500"
              >
                <option value="RESPONSE_TIME">Response Time (ms)</option>
                <option value="ERROR_RATE">Error Rate (%)</option>
                <option value="AVAILABILITY">Availability Uptime (%)</option>
                <option value="FAILURE_COUNT">Consecutive Failures</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Severity</label>
              <select
                value={formData.severity}
                onChange={(e) => setFormData({ ...formData, severity: e.target.value as IncidentSeverity })}
                className="w-full bg-navy-950/60 border border-navy-750 text-xs rounded-lg px-3 py-2 text-slate-200 focus:outline-none focus:border-electric-500"
              >
                <option value="CRITICAL">CRITICAL</option>
                <option value="HIGH">HIGH</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="LOW">LOW</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <Input
              label="Threshold Value *"
              type="number"
              step="any"
              value={formData.thresholdValue}
              onChange={(e) => setFormData({ ...formData, thresholdValue: Number(e.target.value) })}
              required
            />

            <Input
              label="Evaluation Window (Minutes)"
              type="number"
              min={1}
              max={60}
              value={formData.evaluationWindowMinutes || 5}
              onChange={(e) =>
                setFormData({ ...formData, evaluationWindowMinutes: Number(e.target.value) })
              }
            />
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-navy-750">
            <Button variant="ghost" type="button" onClick={() => setIsCreateModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" isLoading={createRuleMutation.isPending}>
              Create Alert Rule
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Rule Confirmation Modal */}
      <Modal
        isOpen={!!ruleToDelete}
        onClose={() => setRuleToDelete(null)}
        title="Delete Alert Rule"
        subtitle="This action will remove threshold monitoring for this rule"
        maxWidth="sm"
      >
        <div className="space-y-4">
          <div className="flex items-center gap-3 p-3 bg-rose-500/10 border border-rose-500/20 rounded-lg text-rose-300 text-xs">
            <AlertTriangle className="w-5 h-5 flex-shrink-0 text-rose-400" />
            <div>Are you sure you want to delete <strong>{ruleToDelete?.name}</strong>?</div>
          </div>
          <div className="flex justify-end gap-2">
            <Button variant="ghost" onClick={() => setRuleToDelete(null)}>
              Cancel
            </Button>
            <Button
              variant="danger"
              isLoading={deleteMutation.isPending}
              onClick={() => ruleToDelete && deleteMutation.mutate(ruleToDelete.id)}
            >
              Delete
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
