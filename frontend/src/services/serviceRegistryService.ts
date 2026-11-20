import api from './api';
import { CreateServiceRequest, ServiceItem } from '../types/service';
import { MonitorStatus } from '../types/monitor';
import { PaginatedResponse } from './monitorService';

export const serviceRegistryService = {
  async getAllServices(params?: {
    environment?: string;
    status?: MonitorStatus;
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<ServiceItem>> {
    const response = await api.get('/services', { params });
    return response.data.data;
  },

  async getServiceById(id: string): Promise<ServiceItem> {
    const response = await api.get(`/services/${id}`);
    return response.data.data;
  },

  async registerService(data: CreateServiceRequest): Promise<ServiceItem> {
    const response = await api.post('/services', data);
    return response.data.data;
  },

  async deleteService(id: string): Promise<void> {
    await api.delete(`/services/${id}`);
  },
};

