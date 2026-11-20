import api from './api';
import {
  CreateIncidentRequest,
  Incident,
  IncidentComment,
  IncidentEvent,
  IncidentSeverity,
  IncidentStatus,
  UpdateIncidentStatusRequest,
} from '../types/incident';
import { PaginatedResponse } from './monitorService';

export const incidentService = {
  async getAllIncidents(params?: {
    status?: IncidentStatus;
    severity?: IncidentSeverity;
    search?: string;
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<Incident>> {
    const response = await api.get('/incidents', { params });
    return response.data.data;
  },

  async getIncidentById(id: string): Promise<Incident> {
    const response = await api.get(`/incidents/${id}`);
    return response.data.data;
  },

  async getIncidentByNumber(incidentNumber: string): Promise<Incident> {
    const response = await api.get(`/incidents/by-number/${incidentNumber}`);
    return response.data.data;
  },

  async createIncident(data: CreateIncidentRequest): Promise<Incident> {
    const response = await api.post('/incidents', data);
    return response.data.data;
  },

  async updateIncident(id: string, data: UpdateIncidentStatusRequest): Promise<Incident> {
    const response = await api.patch(`/incidents/${id}`, data);
    return response.data.data;
  },

  async addComment(id: string, commentText: string): Promise<IncidentComment> {
    const response = await api.post(`/incidents/${id}/comments`, { commentText });
    return response.data.data;
  },

  async getComments(id: string): Promise<IncidentComment[]> {
    const response = await api.get(`/incidents/${id}/comments`);
    return response.data.data;
  },

  async getTimeline(id: string): Promise<IncidentEvent[]> {
    const response = await api.get(`/incidents/${id}/timeline`);
    return response.data.data;
  },
};

