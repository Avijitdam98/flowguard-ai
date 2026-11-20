import api from './api';
import {
  ApiMonitor,
  MonitoringResult,
  CreateApiMonitorRequest,
  UpdateApiMonitorRequest,
  EnvironmentType,
  MonitorStatus,
} from '../types/monitor';

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export const monitorService = {
  async getAllMonitors(params?: {
    environment?: EnvironmentType;
    status?: MonitorStatus;
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<ApiMonitor>> {
    const response = await api.get('/monitors', { params });
    return response.data.data;
  },

  async getMonitorById(id: string): Promise<ApiMonitor> {
    const response = await api.get(`/monitors/${id}`);
    return response.data.data;
  },

  async createMonitor(data: CreateApiMonitorRequest): Promise<ApiMonitor> {
    const response = await api.post('/monitors', data);
    return response.data.data;
  },

  async updateMonitor(id: string, data: UpdateApiMonitorRequest): Promise<ApiMonitor> {
    const response = await api.put(`/monitors/${id}`, data);
    return response.data.data;
  },

  async deleteMonitor(id: string): Promise<void> {
    await api.delete(`/monitors/${id}`);
  },

  async toggleMonitor(id: string): Promise<ApiMonitor> {
    const response = await api.patch(`/monitors/${id}/toggle`);
    return response.data.data;
  },

  async triggerProbe(id: string): Promise<MonitoringResult> {
    const response = await api.post(`/monitors/${id}/probe`);
    return response.data.data;
  },

  async getMonitorHistory(
    id: string,
    params?: { page?: number; size?: number }
  ): Promise<PaginatedResponse<MonitoringResult>> {
    const response = await api.get(`/monitors/${id}/history`, { params });
    return response.data.data;
  },
};

