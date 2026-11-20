import api from './api';
import { IngestLogRequest, LogEntry, LogLevel, LogStats } from '../types/log';
import { PaginatedResponse } from './monitorService';

export const logService = {
  async searchLogs(params?: {
    serviceName?: string;
    level?: LogLevel;
    search?: string;
    startTime?: string;
    endTime?: string;
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<LogEntry>> {
    const response = await api.get('/logs', { params });
    return response.data.data;
  },

  async getLogById(id: string): Promise<LogEntry> {
    const response = await api.get(`/logs/${id}`);
    return response.data.data;
  },

  async ingestLog(data: IngestLogRequest): Promise<LogEntry> {
    const response = await api.post('/logs', data);
    return response.data.data;
  },

  async getDistinctServices(): Promise<string[]> {
    const response = await api.get('/logs/services');
    return response.data.data;
  },

  async getLogStats(): Promise<LogStats> {
    const response = await api.get('/logs/stats');
    return response.data.data;
  },
};

