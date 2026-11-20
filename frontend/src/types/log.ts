export type LogLevel = 'TRACE' | 'DEBUG' | 'INFO' | 'WARN' | 'ERROR' | 'FATAL';

export interface LogEntry {
  id: string;
  serviceId?: string;
  serviceName: string;
  environment: string;
  logLevel: LogLevel;
  message: string;
  traceId?: string;
  correlationId?: string;
  exceptionDetails?: string;
  loggerName?: string;
  timestamp: string;
  metadata?: string;
}

export interface IngestLogRequest {
  serviceId?: string;
  serviceName: string;
  environment?: string;
  logLevel: LogLevel;
  message: string;
  traceId?: string;
  correlationId?: string;
  exceptionDetails?: string;
  loggerName?: string;
  timestamp?: string;
  metadata?: string;
}

export interface LogStats {
  totalLogs: number;
  errorCount: number;
  warningCount: number;
  infoCount: number;
  countByLevel: Record<string, number>;
}

