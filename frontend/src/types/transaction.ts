export type TransactionStatus = 'SUCCESS' | 'FAILED' | 'PROCESSING' | 'PENDING';
export type StepStatus = 'SUCCESS' | 'FAILED' | 'SKIPPED';

export interface TransactionStep {
  id: string;
  transactionId?: string;
  serviceName: string;
  stepName: string;
  stepOrder: number;
  status: StepStatus;
  durationMs: number;
  errorMessage?: string;
  payloadSnapshot?: string;
  executedAt: string;
}

export interface Transaction {
  id: string;
  transactionId: string;
  correlationId: string;
  requestId?: string;
  transactionStatus: TransactionStatus;
  startTime: string;
  endTime?: string;
  totalDurationMs: number;
  rootServiceName?: string;
  failureReason?: string;
  metadata?: string;
  createdAt: string;
  steps: TransactionStep[];
}

export interface CreateTransactionRequest {
  transactionId: string;
  correlationId: string;
  requestId?: string;
  transactionStatus: TransactionStatus;
  rootServiceName?: string;
  failureReason?: string;
  metadata?: string;
  totalDurationMs?: number;
}

export interface RecordStepRequest {
  serviceName: string;
  stepName: string;
  stepOrder: number;
  status: StepStatus;
  durationMs: number;
  errorMessage?: string;
  payloadSnapshot?: string;
}

