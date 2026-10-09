import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/authApi';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('careerpulse_user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('careerpulse_token'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const handleUnauthorized = () => {
      setUser(null);
      setToken(null);
    };

    window.addEventListener('auth:unauthorized', handleUnauthorized);

    // Verify token validity on load if token exists
    if (token) {
      authApi.getCurrentUser()
        .then((userData) => {
          setUser(userData);
          localStorage.setItem('careerpulse_user', JSON.stringify(userData));
        })
        .catch(() => {
          localStorage.removeItem('careerpulse_token');
          localStorage.removeItem('careerpulse_user');
          setUser(null);
          setToken(null);
        })
        .finally(() => {
          setLoading(false);
        });
    } else {
      setLoading(false);
    }

    return () => {
      window.removeEventListener('auth:unauthorized', handleUnauthorized);
    };
  }, [token]);

  const login = async (credentials) => {
    const response = await authApi.login(credentials);
    const { token: jwtToken, user: userData } = response;
    localStorage.setItem('careerpulse_token', jwtToken);
    localStorage.setItem('careerpulse_user', JSON.stringify(userData));
    setToken(jwtToken);
    setUser(userData);
    return response;
  };

  const register = async (userData) => {
    const response = await authApi.register(userData);
    const { token: jwtToken, user: newUser } = response;
    localStorage.setItem('careerpulse_token', jwtToken);
    localStorage.setItem('careerpulse_user', JSON.stringify(newUser));
    setToken(jwtToken);
    setUser(newUser);
    return response;
  };

  const logout = () => {
    localStorage.removeItem('careerpulse_token');
    localStorage.removeItem('careerpulse_user');
    setToken(null);
    setUser(null);
  };

  const updateProfile = async (profileData) => {
    const response = await authApi.updateProfile(profileData);
    localStorage.setItem('careerpulse_token', response.token);
    localStorage.setItem('careerpulse_user', JSON.stringify(response.user));
    setToken(response.token);
    setUser(response.user);
    return response.user;
  };

  const value = {
    user,
    token,
    isAuthenticated: !!token,
    loading,
    login,
    register,
    logout,
    updateProfile,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

