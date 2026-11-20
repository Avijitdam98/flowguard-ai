import { IncidentSeverity } from './incident';

export type TargetType = 'API_MONITOR' | 'SERVICE' | 'GLOBAL';
export type MetricType = 'RESPONSE_TIME' | 'ERROR_RATE' | 'AVAILABILITY' | 'FAILURE_COUNT';
export type ComparisonOperator = 'GREATER_THAN' | 'LESS_THAN' | 'EQUALS' | 'NOT_EQUALS';

export interface AlertRule {
  id: string;
  name: string;
  description?: string;
  targetType: TargetType;
  targetId?: string;
  metricType: MetricType;
  comparisonOperator: ComparisonOperator;
  thresholdValue: number;
  evaluationWindowMinutes: number;
  severity: IncidentSeverity;
  isEnabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAlertRuleRequest {
  name: string;
  description?: string;
  targetType: TargetType;
  targetId?: string;
  metricType: MetricType;
  comparisonOperator: ComparisonOperator;
  thresholdValue: number;
  evaluationWindowMinutes?: number;
  severity?: IncidentSeverity;
  isEnabled?: boolean;
}

export interface AlertHistory {
  id: string;
  alertRuleId: string;
  alertRuleName?: string;
  triggeredValue: number;
  message: string;
  severity: IncidentSeverity;
  isAcknowledged: boolean;
  acknowledgedBy?: string;
  acknowledgedByName?: string;
  acknowledgedAt?: string;
  triggeredAt: string;
}

export interface NotificationItem {
  id: string;
  recipientEmail: string;
  channel: string;
  title: string;
  message?: string;
  status: string;
  sentAt: string;
}

