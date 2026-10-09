import { apiClient } from './client';

export const companiesApi = {
  getAll: () => apiClient('/companies'),
  getById: (id) => apiClient(`/companies/${id}`),
  create: (data) => apiClient('/companies', { method: 'POST', body: data }),
  update: (id, data) => apiClient(`/companies/${id}`, { method: 'PUT', body: data }),
  delete: (id) => apiClient(`/companies/${id}`, { method: 'DELETE' }),
};

