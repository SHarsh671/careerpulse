import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { dashboardApi } from '../api/dashboardApi';
import StatCard from '../components/StatCard';
import StatusBadge from '../components/StatusBadge';
import LoadingSpinner from '../components/LoadingSpinner';
import EmptyState from '../components/EmptyState';
import {
  Briefcase,
  Send,
  Calendar,
  Award,
  TrendingUp,
  Percent,
  Plus,
  ArrowRight,
  AlertCircle
} from 'lucide-react';

export default function DashboardPage() {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchStats = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await dashboardApi.getStats();
      setStats(data);
    } catch (err) {
      setError(err.message || 'Failed to load dashboard metrics.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  if (loading) {
    return <LoadingSpinner message="Calculating your job search metrics..." />;
  }

  if (error) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
        <AlertCircle size={36} color="#ef4444" style={{ marginBottom: '1rem' }} />
        <h3>Failed to load dashboard</h3>
        <p style={{ color: 'var(--text-muted)', margin: '0.5rem 0 1.5rem' }}>{error}</p>
        <button className="btn btn-primary" onClick={fetchStats}>Retry</button>
      </div>
    );
  }

  return (
    <div>
      {/* Header */}
      <div className="flex-between mb-lg" style={{ flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>Welcome back, {user?.name?.split(' ')[0]}!</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
            Here is your job hunt progress and application pipeline summary.
          </p>
        </div>
        <Link to="/applications/new" className="btn btn-primary">
          <Plus size={18} /> New Application
        </Link>
      </div>

      {/* Main KPI Cards */}
      <div className="stat-grid">
        <StatCard
          label="Total Tracked"
          value={stats?.totalApplications || 0}
          icon={Briefcase}
          color="#2563eb"
          bg="#eff6ff"
        />
        <StatCard
          label="Applied / Sent"
          value={stats?.appliedApplications || 0}
          icon={Send}
          color="#0284c7"
          bg="#f0f9ff"
        />
        <StatCard
          label="Interviews Scheduled"
          value={stats?.interviewCount || 0}
          icon={Calendar}
          color="#7c3aed"
          bg="#f5f3ff"
        />
        <StatCard
          label="Job Offers"
          value={stats?.offers || 0}
          icon={Award}
          color="#059669"
          bg="#ecfdf5"
        />
        <StatCard
          label="Interview Rate"
          value={`${stats?.interviewRate || 0}%`}
          icon={TrendingUp}
          color="#d97706"
          bg="#fffbeb"
        />
        <StatCard
          label="Offer Rate"
          value={`${stats?.offerRate || 0}%`}
          icon={Percent}
          color="#10b981"
          bg="#d1fae5"
        />
      </div>

      {/* Application Pipeline Status Breakdown */}
      <div className="card mb-lg">
        <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1.25rem' }}>Pipeline Breakdown</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))', gap: '1rem' }}>
          {stats?.statusBreakdown && Object.entries(stats.statusBreakdown).map(([statusKey, count]) => (
            <div key={statusKey} style={{ padding: '0.85rem', backgroundColor: '#f8fafc', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
              <div style={{ marginBottom: '0.5rem' }}>
                <StatusBadge status={statusKey} />
              </div>
              <div style={{ fontSize: '1.4rem', fontWeight: 700, color: 'var(--text-main)' }}>
                {count}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Recent Applications Section */}
      <div className="card">
        <div className="flex-between mb-md">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>Recent Applications</h3>
          <Link to="/applications" className="nav-link" style={{ fontSize: '0.875rem', fontWeight: 600 }}>
            View All <ArrowRight size={16} />
          </Link>
        </div>

        {(!stats?.recentApplications || stats.recentApplications.length === 0) ? (
          <EmptyState
            title="No applications tracked yet"
            description="Start logging your job applications to see real-time statistics and progression."
            actionLabel="Add Your First Application"
            onAction={() => window.location.href = '/applications/new'}
          />
        ) : (
          <div className="table-responsive">
            <table className="table">
              <thead>
                <tr>
                  <th>Job Title</th>
                  <th>Company</th>
                  <th>Location</th>
                  <th>Status</th>
                  <th>Applied Date</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {stats.recentApplications.map((app) => (
                  <tr key={app.id}>
                    <td style={{ fontWeight: 600 }}>
                      <Link to={`/applications/${app.id}`}>{app.jobTitle}</Link>
                    </td>
                    <td>{app.company?.name || '—'}</td>
                    <td>{app.location || 'Remote / Unspecified'}</td>
                    <td><StatusBadge status={app.status} /></td>
                    <td>{app.appliedDate || 'Not specified'}</td>
                    <td>
                      <Link to={`/applications/${app.id}`} className="btn btn-secondary btn-sm">
                        View
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

