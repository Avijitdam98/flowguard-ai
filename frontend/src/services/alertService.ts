import api from './api';
import {
  AlertHistory,
  AlertRule,
  CreateAlertRuleRequest,
  NotificationItem,
} from '../types/alert';
import { PaginatedResponse } from './monitorService';

export const alertService = {
  async getAllRules(params?: {
    isEnabled?: boolean;
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<AlertRule>> {
    const response = await api.get('/alerts/rules', { params });
    return response.data.data;
  },

  async getRuleById(id: string): Promise<AlertRule> {
    const response = await api.get(`/alerts/rules/${id}`);
    return response.data.data;
  },

  async createRule(data: CreateAlertRuleRequest): Promise<AlertRule> {
    const response = await api.post('/alerts/rules', data);
    return response.data.data;
  },

  async toggleRule(id: string): Promise<AlertRule> {
    const response = await api.patch(`/alerts/rules/${id}/toggle`);
    return response.data.data;
  },

  async deleteRule(id: string): Promise<void> {
    await api.delete(`/alerts/rules/${id}`);
  },

  async getAlertHistory(params?: {
    isAcknowledged?: boolean;
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<AlertHistory>> {
    const response = await api.get('/alerts/history', { params });
    return response.data.data;
  },

  async acknowledgeAlert(id: string): Promise<AlertHistory> {
    const response = await api.patch(`/alerts/history/${id}/acknowledge`);
    return response.data.data;
  },

  async getUnacknowledgedCount(): Promise<number> {
    const response = await api.get('/alerts/unacknowledged/count');
    return response.data.data.count;
  },

  async getNotifications(params?: {
    email?: string;
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<NotificationItem>> {
    const response = await api.get('/alerts/notifications', { params });
    return response.data.data;
  },
};

