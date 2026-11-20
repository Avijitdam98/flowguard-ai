import api from './api';
import { AiChatRequest, AiChatResponse, AiRootCauseResponse } from '../types/ai';

export const aiService = {
  async getIncidentRca(incidentId: string): Promise<AiRootCauseResponse> {
    const response = await api.post(`/ai/incidents/${incidentId}/analysis`);
    return response.data.data;
  },

  async chatWithCopilot(data: AiChatRequest): Promise<AiChatResponse> {
    const response = await api.post('/ai/assistant/chat', data);
    return response.data.data;
  },
};

