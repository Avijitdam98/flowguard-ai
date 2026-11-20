export type IncidentSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
export type IncidentStatus = 'DETECTED' | 'ACKNOWLEDGED' | 'INVESTIGATING' | 'RESOLVED' | 'CLOSED';

export interface IncidentComment {
  id: string;
  incidentId: string;
  userId?: string;
  userName?: string;
  commentText: string;
  createdAt: string;
}

export interface IncidentEvent {
  id: string;
  incidentId: string;
  eventType: string;
  eventDescription: string;
  actorId?: string;
  actorName?: string;
  createdAt: string;
}

export interface Incident {
  id: string;
  incidentNumber: string;
  title: string;
  description: string;
  severity: IncidentSeverity;
  status: IncidentStatus;
  affectedServiceId?: string;
  relatedApiId?: string;
  assignedEngineerId?: string;
  assignedEngineerName?: string;
  resolutionNotes?: string;
  resolvedAt?: string;
  createdAt: string;
  updatedAt: string;
  comments?: IncidentComment[];
  events?: IncidentEvent[];
}

export interface CreateIncidentRequest {
  title: string;
  description: string;
  severity: IncidentSeverity;
  affectedServiceId?: string;
  relatedApiId?: string;
  assignedEngineerId?: string;
  assignedEngineerName?: string;
}

export interface UpdateIncidentStatusRequest {
  status?: IncidentStatus;
  severity?: IncidentSeverity;
  assignedEngineerId?: string;
  assignedEngineerName?: string;
  resolutionNotes?: string;
}

