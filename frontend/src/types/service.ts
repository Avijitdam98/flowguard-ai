import { MonitorStatus } from './monitor';

export interface ServiceItem {
  id: string;
  teamId?: string;
  name: string;
  slug: string;
  description?: string;
  environment: string;
  currentStatus: MonitorStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateServiceRequest {
  name: string;
  slug: string;
  description?: string;
  environment?: string;
  currentStatus?: MonitorStatus;
  teamId?: string;
}

