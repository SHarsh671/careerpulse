/**
 * Centralized API client for all backend REST communications.
 * Automatically attaches JWT authentication header and standardizes error handling.
 */

const BASE_URL = import.meta.env.VITE_API_URL || '/api';

export async function apiClient(endpoint, { method = 'GET', body, headers = {}, ...customConfig } = {}) {
  const token = localStorage.getItem('careerpulse_token');

  const config = {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...headers,
    },
    ...customConfig,
  };

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  if (body) {
    config.body = JSON.stringify(body);
  }

  let response;
  try {
    response = await fetch(`${BASE_URL}${endpoint}`, config);
  } catch (networkError) {
    throw new Error('Unable to connect to the server. Please check your network or try again later.');
  }

  // Handle 204 No Content
  if (response.status === 204) {
    return null;
  }

  let data;
  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    data = await response.json();
  } else {
    data = await response.text();
  }

  if (!response.ok) {
    if (response.status === 401) {
      // Clear token on authentication failure
      localStorage.removeItem('careerpulse_token');
      localStorage.removeItem('careerpulse_user');
      window.dispatchEvent(new Event('auth:unauthorized'));
    }

    let errorMessage = 'An error occurred while processing your request.';
    if (data && typeof data === 'object') {
      if (data.errors && Object.keys(data.errors).length > 0) {
        // Validation error messages
        errorMessage = Object.values(data.errors).join(', ');
      } else if (data.message) {
        errorMessage = data.message;
      }
    } else if (typeof data === 'string' && data.length > 0) {
      errorMessage = data;
    }

    const error = new Error(errorMessage);
    error.status = response.status;
    error.data = data;
    throw error;
  }

  return data;
}

