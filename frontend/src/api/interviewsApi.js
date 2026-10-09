import { apiClient } from './client';

export const interviewsApi = {
  getByApplicationId: (applicationId) => apiClient(`/applications/${applicationId}/interviews`),
  create: (applicationId, data) => apiClient(`/applications/${applicationId}/interviews`, { method: 'POST', body: data }),
  getById: (id) => apiClient(`/interviews/${id}`),
  update: (id, data) => apiClient(`/interviews/${id}`, { method: 'PUT', body: data }),
  delete: (id) => apiClient(`/interviews/${id}`, { method: 'DELETE' }),
};

