export type MonitorStatus = 'HEALTHY' | 'DEGRADED' | 'DOWN' | 'UNKNOWN';
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH' | 'HEAD' | 'OPTIONS';
export type EnvironmentType = 'PRODUCTION' | 'STAGING' | 'DEVELOPMENT';

export interface ApiMonitor {
  id: string;
  name: string;
  baseUrl: string;
  endpointPath: string;
  fullUrl: string;
  httpMethod: HttpMethod;
  environment: EnvironmentType;
  requestHeaders?: string;
  requestBody?: string;
  authConfig?: string;
  monitoringIntervalSeconds: number;
  timeoutMs: number;
  expectedHttpStatus: number;
  alertThresholdMs: number;
  isActive: boolean;
  currentStatus: MonitorStatus;
  lastCheckedAt?: string;
  teamId?: string;
  createdAt: string;
  updatedAt: string;
}

export interface MonitoringResult {
  id: string;
  monitorId: string;
  statusCode: number;
  responseTimeMs: number;
  isSuccessful: boolean;
  errorMessage?: string;
  responseHeaders?: string;
  responseBodySample?: string;
  checkedAt: string;
}

export interface CreateApiMonitorRequest {
  name: string;
  baseUrl: string;
  endpointPath: string;
  httpMethod: HttpMethod;
  environment: EnvironmentType;
  requestHeaders?: string;
  requestBody?: string;
  authConfig?: string;
  monitoringIntervalSeconds?: number;
  timeoutMs?: number;
  expectedHttpStatus?: number;
  alertThresholdMs?: number;
  isActive?: boolean;
}

export interface UpdateApiMonitorRequest {
  name?: string;
  baseUrl?: string;
  endpointPath?: string;
  httpMethod?: HttpMethod;
  environment?: EnvironmentType;
  requestHeaders?: string;
  requestBody?: string;
  authConfig?: string;
  monitoringIntervalSeconds?: number;
  timeoutMs?: number;
  expectedHttpStatus?: number;
  alertThresholdMs?: number;
  isActive?: boolean;
}

