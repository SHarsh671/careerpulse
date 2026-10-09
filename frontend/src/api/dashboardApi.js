import { apiClient } from './client';

export const dashboardApi = {
  getStats: () => apiClient('/dashboard'),
};

