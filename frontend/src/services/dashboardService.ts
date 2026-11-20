import api from './api';
import { DashboardMetrics, TimeSeriesPoint } from '../types/dashboard';

export const dashboardService = {
  async getDashboardMetrics(timeRange: string = '24h'): Promise<DashboardMetrics> {
    const response = await api.get('/dashboard/metrics', {
      params: { timeRange },
    });
    return response.data.data;
  },

  async getTrafficTrend(timeRange: string = '24h'): Promise<TimeSeriesPoint[]> {
    const response = await api.get('/dashboard/traffic', {
      params: { timeRange },
    });
    return response.data.data;
  },

  async getLatencyTrend(timeRange: string = '24h'): Promise<TimeSeriesPoint[]> {
    const response = await api.get('/dashboard/latency', {
      params: { timeRange },
    });
    return response.data.data;
  },
};

