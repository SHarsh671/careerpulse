import { apiClient } from './client';

export const applicationsApi = {
  getAll: (params = {}) => {
    const query = new URLSearchParams();
    if (params.status) query.append('status', params.status);
    if (params.companyId) query.append('companyId', params.companyId);
    if (params.location) query.append('location', params.location);
    if (params.search) query.append('search', params.search);
    if (params.page !== undefined) query.append('page', params.page);
    if (params.size !== undefined) query.append('size', params.size);
    if (params.sort) query.append('sort', params.sort);

    const queryString = query.toString();
    return apiClient(`/applications${queryString ? `?${queryString}` : ''}`);
  },
  getById: (id) => apiClient(`/applications/${id}`),
  create: (data) => apiClient('/applications', { method: 'POST', body: data }),
  update: (id, data) => apiClient(`/applications/${id}`, { method: 'PUT', body: data }),
  updateStatus: (id, status) => apiClient(`/applications/${id}/status`, { method: 'PATCH', body: { status } }),
  delete: (id) => apiClient(`/applications/${id}`, { method: 'DELETE' }),
};

