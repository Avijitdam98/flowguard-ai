export interface TimeSeriesPoint {
  timestamp: string;
  value: number;
  secondaryValue?: number;
  label?: string;
}

export interface FailingApiSummary {
  id: string;
  name: string;
  endpoint: string;
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH' | 'HEAD';
  failureCount: number;
  failureRate: number;
  lastStatusCode: number;
  lastErrorMessage: string;
}

export interface ServiceHealthSummary {
  id: string;
  serviceName: string;
  environment: string;
  healthStatus: 'HEALTHY' | 'DEGRADED' | 'UNHEALTHY' | 'UNKNOWN';
  averageLatencyMs: number;
  uptimePercentage: number;
  lastHeartbeat: string;
}

export interface RecentIncidentSummary {
  id: string;
  incidentNumber: string;
  title: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  status: 'DETECTED' | 'ACKNOWLEDGED' | 'INVESTIGATING' | 'RESOLVED' | 'CLOSED';
  affectedService: string;
  createdAt: string;
}

export interface RecentTransactionSummary {
  id: string;
  transactionId: string;
  correlationId: string;
  status: 'SUCCESS' | 'FAILED' | 'PENDING' | 'PROCESSING';
  durationMs: number;
  rootService: string;
  startTime: string;
}

export interface DashboardMetrics {
  totalApiRequests: number;
  successfulRequests: number;
  failedRequests: number;
  averageResponseTimeMs: number;
  p95ResponseTimeMs: number;
  apiAvailabilityPercentage: number;
  activeIncidents: number;
  transactionSuccessRate: number;
  trafficTrend: TimeSeriesPoint[];
  latencyTrend: TimeSeriesPoint[];
  errorRateTrend: TimeSeriesPoint[];
  serviceHealthDistribution: Record<string, number>;
  statusDistribution: Record<string, number>;
  topFailingApis: FailingApiSummary[];
  slowestApis: FailingApiSummary[];
  serviceHealthOverview: ServiceHealthSummary[];
  recentIncidents: RecentIncidentSummary[];
  recentTransactions: RecentTransactionSummary[];
}

