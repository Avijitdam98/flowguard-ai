import api from './api';
import {
  Transaction,
  TransactionStep,
  CreateTransactionRequest,
  RecordStepRequest,
  TransactionStatus,
} from '../types/transaction';
import { PaginatedResponse } from './monitorService';

export const transactionService = {
  async getAllTransactions(params?: {
    search?: string;
    status?: TransactionStatus;
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<PaginatedResponse<Transaction>> {
    const response = await api.get('/transactions', { params });
    return response.data.data;
  },

  async getTransactionById(id: string): Promise<Transaction> {
    const response = await api.get(`/transactions/${id}`);
    return response.data.data;
  },

  async getTransactionByTxId(txId: string): Promise<Transaction> {
    const response = await api.get(`/transactions/by-txid/${txId}`);
    return response.data.data;
  },

  async createTransaction(data: CreateTransactionRequest): Promise<Transaction> {
    const response = await api.post('/transactions', data);
    return response.data.data;
  },

  async recordStep(transactionId: string, data: RecordStepRequest): Promise<TransactionStep> {
    const response = await api.post(`/transactions/${transactionId}/steps`, data);
    return response.data.data;
  },
};

