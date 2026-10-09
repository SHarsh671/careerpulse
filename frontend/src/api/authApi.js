import { apiClient } from './client';

export const authApi = {
  register: (data) => apiClient('/auth/register', { method: 'POST', body: data }),
  login: (data) => apiClient('/auth/login', { method: 'POST', body: data }),
  getCurrentUser: () => apiClient('/auth/me'),
  updateProfile: (data) => apiClient('/auth/profile', { method: 'PUT', body: data }),
};

