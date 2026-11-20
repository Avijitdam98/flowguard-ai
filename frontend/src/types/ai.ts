export interface AiRootCauseResponse {
  incidentId: string;
  incidentNumber: string;
  summary: string;
  confidenceScore: number;
  potentialRootCauses: string[];
  affectedServices: string[];
  relevantErrorPatterns: string[];
  recommendedInvestigationSteps: string[];
  suggestedRemediationActions: string[];
  supportingEvidence?: string;
  modelUsed: string;
  disclaimer: string;
}

export interface AiChatRequest {
  prompt: string;
  conversationHistory?: Array<{ role: string; content: string }>;
  contextScope?: string;
}

export interface AiChatResponse {
  response: string;
  model: string;
  referencedEntities: string[];
  suggestedNextPrompts: string[];
  generatedAt: string;
  safetyNotice: string;
}

