import React, { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { applicationsApi } from '../api/applicationsApi';
import { companiesApi } from '../api/companiesApi';
import StatusBadge from '../components/StatusBadge';
import Pagination from '../components/Pagination';
import LoadingSpinner from '../components/LoadingSpinner';
import EmptyState from '../components/EmptyState';
import Modal from '../components/Modal';
import {
  Plus,
  Search,
  Filter,
  Trash2,
  ExternalLink,
  Calendar,
  AlertCircle
} from 'lucide-react';

const STATUS_OPTIONS = [
  { value: '', label: 'All Statuses' },
  { value: 'SAVED', label: 'Saved' },
  { value: 'APPLIED', label: 'Applied' },
  { value: 'OA', label: 'Online Assessment' },
  { value: 'INTERVIEW', label: 'Interview' },
  { value: 'FINAL_INTERVIEW', label: 'Final Interview' },
  { value: 'OFFER', label: 'Offer' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'WITHDRAWN', label: 'Withdrawn' },
];

export default function ApplicationsPage() {
  const navigate = useNavigate();
  const [applications, setApplications] = useState([]);
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Filtering & Pagination State
  const [search, setSearch] = useState('');
  const [location, setLocation] = useState('');
  const [status, setStatus] = useState('');
  const [companyId, setCompanyId] = useState('');
  const [sort, setSort] = useState('appliedDate,desc');
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Deletion modal state
  const [appToDelete, setAppToDelete] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const fetchCompanies = async () => {
    try {
      const data = await companiesApi.getAll();
      setCompanies(data);
    } catch {
      // Ignore company load failures on filter dropdown
    }
  };

  const fetchApplications = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const response = await applicationsApi.getAll({
        search: search.trim() || undefined,
        location: location.trim() || undefined,
        status: status || undefined,
        companyId: companyId || undefined,
        sort,
        page,
        size
      });

      setApplications(response.content || []);
      setTotalPages(response.totalPages || 0);
      setTotalElements(response.totalElements || 0);
    } catch (err) {
      setError(err.message || 'Failed to load applications.');
    } finally {
      setLoading(false);
    }
  }, [search, location, status, companyId, sort, page, size]);

  useEffect(() => {
    fetchCompanies();
  }, []);

  useEffect(() => {
    fetchApplications();
  }, [fetchApplications]);

  const handleStatusChange = async (appId, newStatus) => {
    try {
      await applicationsApi.updateStatus(appId, newStatus);
      // Update local state without full reload
      setApplications((prev) =>
        prev.map((app) => (app.id === appId ? { ...app, status: newStatus } : app))
      );
    } catch (err) {
      alert(err.message || 'Failed to update status');
    }
  };

  const confirmDelete = async () => {
    if (!appToDelete) return;
    setDeleteLoading(true);
    try {
      await applicationsApi.delete(appToDelete.id);
      setAppToDelete(null);
      fetchApplications();
    } catch (err) {
      alert(err.message || 'Failed to delete application.');
    } finally {
      setDeleteLoading(false);
    }
  };

  const handleResetFilters = () => {
    setSearch('');
    setLocation('');
    setStatus('');
    setCompanyId('');
    setSort('appliedDate,desc');
    setPage(0);
  };

  return (
    <div>
      {/* Header */}
      <div className="flex-between mb-lg" style={{ flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>Job Applications</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
            Filter, track, and update all your job opportunities in one place.
          </p>
        </div>
        <Link to="/applications/new" className="btn btn-primary">
          <Plus size={18} /> New Application
        </Link>
      </div>

      {/* Filter and Search Bar */}
      <div className="card mb-lg" style={{ padding: '1.25rem' }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '0.85rem', alignItems: 'center' }}>
          {/* Search Box */}
          <div style={{ position: 'relative' }}>
            <Search size={16} style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
            <input
              type="text"
              className="form-control"
              placeholder="Search title, company..."
              style={{ paddingLeft: '2.25rem' }}
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
            />
          </div>

          <input
            type="text"
            className="form-control"
            placeholder="Filter by location..."
            value={location}
            onChange={(e) => {
              setLocation(e.target.value);
              setPage(0);
            }}
          />

          {/* Status Filter */}
          <div>
            <select
              className="form-select"
              value={status}
              onChange={(e) => {
                setStatus(e.target.value);
                setPage(0);
              }}
            >
              {STATUS_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
          </div>

          {/* Company Filter */}
          <div>
            <select
              className="form-select"
              value={companyId}
              onChange={(e) => {
                setCompanyId(e.target.value);
                setPage(0);
              }}
            >
              <option value="">All Companies</option>
              {companies.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>

          {/* Sort Filter */}
          <div>
            <select
              className="form-select"
              value={sort}
              onChange={(e) => {
                setSort(e.target.value);
                setPage(0);
              }}
            >
              <option value="appliedDate,desc">Applied Date (Newest)</option>
              <option value="appliedDate,asc">Applied Date (Oldest)</option>
              <option value="jobTitle,asc">Job Title (A-Z)</option>
              <option value="status,asc">Status</option>
              <option value="createdAt,desc">Created Date (Newest)</option>
            </select>
          </div>
        </div>

        {(search || location || status || companyId) && (
          <div style={{ marginTop: '0.85rem', display: 'flex', justifyContent: 'flex-end' }}>
            <button className="btn btn-secondary btn-sm" onClick={handleResetFilters}>
              Clear Filters
            </button>
          </div>
        )}
      </div>

      {/* Main Content Area */}
      {error && (
        <div className="alert alert-danger">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {loading ? (
        <LoadingSpinner message="Loading applications..." />
      ) : applications.length === 0 ? (
        <EmptyState
          title="No applications match your criteria"
          description={
            search || location || status || companyId
              ? 'Try modifying your search or filter settings.'
              : 'Add your first job application to get started.'
          }
          actionLabel={search || location || status || companyId ? 'Reset Filters' : 'Add Application'}
          onAction={search || location || status || companyId ? handleResetFilters : () => navigate('/applications/new')}
        />
      ) : (
        <div className="card">
          <div className="table-responsive">
            <table className="table">
              <thead>
                <tr>
                  <th>Job Title</th>
                  <th>Company</th>
                  <th>Location</th>
                  <th>Status</th>
                  <th>Applied</th>
                  <th>Interviews</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {applications.map((app) => (
                  <tr key={app.id}>
                    <td>
                      <Link to={`/applications/${app.id}`} style={{ fontWeight: 600, color: 'var(--text-main)' }}>
                        {app.jobTitle}
                      </Link>
                      {app.jobUrl && (
                        <a
                          href={app.jobUrl}
                          target="_blank"
                          rel="noreferrer"
                          style={{ marginLeft: '0.4rem', color: 'var(--text-muted)' }}
                          title="Open job posting"
                        >
                          <ExternalLink size={14} style={{ display: 'inline', verticalAlign: 'middle' }} />
                        </a>
                      )}
                    </td>
                    <td>
                      <span style={{ fontWeight: 500 }}>{app.company?.name || '—'}</span>
                    </td>
                    <td style={{ color: 'var(--text-muted)' }}>{app.location || 'Remote / Unspecified'}</td>
                    <td>
                      <select
                        className="form-select"
                        style={{ fontSize: '0.8rem', padding: '0.25rem 0.5rem', width: 'auto' }}
                        value={app.status}
                        onChange={(e) => handleStatusChange(app.id, e.target.value)}
                      >
                        {STATUS_OPTIONS.filter((o) => o.value).map((opt) => (
                          <option key={opt.value} value={opt.value}>{opt.label}</option>
                        ))}
                      </select>
                    </td>
                    <td style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                      {app.appliedDate || '—'}
                    </td>
                    <td>
                      {app.interviewCount > 0 ? (
                        <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem', padding: '0.2rem 0.5rem', backgroundColor: '#eef2ff', color: '#4338ca', borderRadius: '4px', fontSize: '0.8rem', fontWeight: 600 }}>
                          <Calendar size={13} /> {app.interviewCount}
                        </span>
                      ) : (
                        <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>0</span>
                      )}
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: '0.4rem' }}>
                        <Link to={`/applications/${app.id}`} className="btn btn-secondary btn-sm">
                          Details
                        </Link>
                        <button
                          className="btn btn-secondary btn-sm"
                          style={{ color: '#ef4444' }}
                          onClick={() => setAppToDelete(app)}
                          title="Delete application"
                        >
                          <Trash2 size={15} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <Pagination
            page={page}
            totalPages={totalPages}
            totalElements={totalElements}
            onPageChange={(newPage) => setPage(newPage)}
          />
        </div>
      )}

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={!!appToDelete}
        onClose={() => setAppToDelete(null)}
        title="Delete Job Application"
      >
        <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
          Are you sure you want to delete the application for{' '}
          <strong>{appToDelete?.jobTitle}</strong> at <strong>{appToDelete?.company?.name}</strong>?
          This will also remove all scheduled interviews associated with it. This action cannot be undone.
        </p>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
          <button
            className="btn btn-secondary"
            onClick={() => setAppToDelete(null)}
            disabled={deleteLoading}
          >
            Cancel
          </button>
          <button
            className="btn btn-danger"
            onClick={confirmDelete}
            disabled={deleteLoading}
          >
            {deleteLoading ? 'Deleting...' : 'Delete Application'}
          </button>
        </div>
      </Modal>
    </div>
  );
}

