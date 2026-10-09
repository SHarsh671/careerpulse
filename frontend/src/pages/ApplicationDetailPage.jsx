import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { applicationsApi } from '../api/applicationsApi';
import { interviewsApi } from '../api/interviewsApi';
import { companiesApi } from '../api/companiesApi';
import StatusBadge from '../components/StatusBadge';
import LoadingSpinner from '../components/LoadingSpinner';
import Modal from '../components/Modal';
import {
  ArrowLeft,
  Building2,
  MapPin,
  Calendar,
  DollarSign,
  ExternalLink,
  Plus,
  Trash2,
  Edit2,
  Clock,
  User,
  AlertCircle
} from 'lucide-react';

const STATUS_OPTIONS = [
  'SAVED', 'APPLIED', 'OA', 'INTERVIEW', 'FINAL_INTERVIEW', 'OFFER', 'REJECTED', 'WITHDRAWN'
];

const INTERVIEW_TYPES = [
  'OA', 'PHONE', 'TECHNICAL', 'BEHAVIORAL', 'FINAL'
];

export default function ApplicationDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [application, setApplication] = useState(null);
  const [interviews, setInterviews] = useState([]);
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Application Edit Modal State
  const [showEditAppModal, setShowEditAppModal] = useState(false);
  const [editJobTitle, setEditJobTitle] = useState('');
  const [editCompanyId, setEditCompanyId] = useState('');
  const [editLocation, setEditLocation] = useState('');
  const [editJobUrl, setEditJobUrl] = useState('');
  const [editSalaryMin, setEditSalaryMin] = useState('');
  const [editSalaryMax, setEditSalaryMax] = useState('');
  const [editAppliedDate, setEditAppliedDate] = useState('');
  const [editDeadline, setEditDeadline] = useState('');
  const [editNotes, setEditNotes] = useState('');
  const [editStatus, setEditStatus] = useState('APPLIED');
  const [updatingApp, setUpdatingApp] = useState(false);
  const [editAppError, setEditAppError] = useState('');

  // Interview Modal State (Create & Edit)
  const [showInterviewModal, setShowInterviewModal] = useState(false);
  const [editingInterviewId, setEditingInterviewId] = useState(null);
  const [interviewType, setInterviewType] = useState('TECHNICAL');
  const [interviewDate, setInterviewDate] = useState('');
  const [interviewer, setInterviewer] = useState('');
  const [interviewNotes, setInterviewNotes] = useState('');
  const [interviewResult, setInterviewResult] = useState('PENDING');
  const [savingInterview, setSavingInterview] = useState(false);
  const [interviewError, setInterviewError] = useState('');

  // Deletion modals
  const [showDeleteAppModal, setShowDeleteAppModal] = useState(false);
  const [interviewToDelete, setInterviewToDelete] = useState(null);

  const fetchApplicationData = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const appData = await applicationsApi.getById(id);
      setApplication(appData);

      const interviewData = await interviewsApi.getByApplicationId(id);
      setInterviews(interviewData);

      const compData = await companiesApi.getAll();
      setCompanies(compData);
    } catch (err) {
      setError(err.message || 'Failed to load application details.');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchApplicationData();
  }, [fetchApplicationData]);

  const handleStatusChange = async (newStatus) => {
    try {
      const updated = await applicationsApi.updateStatus(id, newStatus);
      setApplication(updated);
    } catch (err) {
      alert(err.message || 'Failed to update status.');
    }
  };

  const openEditAppModal = () => {
    setEditJobTitle(application.jobTitle || '');
    setEditCompanyId(application.company?.id?.toString() || '');
    setEditLocation(application.location || '');
    setEditJobUrl(application.jobUrl || '');
    setEditSalaryMin(application.salaryMin?.toString() || '');
    setEditSalaryMax(application.salaryMax?.toString() || '');
    setEditAppliedDate(application.appliedDate || '');
    setEditDeadline(application.deadline || '');
    setEditNotes(application.notes || '');
    setEditStatus(application.status || 'APPLIED');
    setEditAppError('');
    setShowEditAppModal(true);
  };

  const handleUpdateApplication = async (e) => {
    e.preventDefault();
    setEditAppError('');

    if (editSalaryMin && editSalaryMax && parseInt(editSalaryMin, 10) > parseInt(editSalaryMax, 10)) {
      setEditAppError('Minimum salary cannot exceed maximum salary.');
      return;
    }

    setUpdatingApp(true);
    try {
      const payload = {
        companyId: parseInt(editCompanyId, 10),
        jobTitle: editJobTitle.trim(),
        location: editLocation.trim() || undefined,
        jobUrl: editJobUrl.trim() || undefined,
        status: editStatus,
        salaryMin: editSalaryMin ? parseInt(editSalaryMin, 10) : undefined,
        salaryMax: editSalaryMax ? parseInt(editSalaryMax, 10) : undefined,
        appliedDate: editAppliedDate || undefined,
        deadline: editDeadline || undefined,
        notes: editNotes.trim() || undefined,
      };

      const updated = await applicationsApi.update(id, payload);
      setApplication(updated);
      setShowEditAppModal(false);
    } catch (err) {
      setEditAppError(err.message || 'Failed to update application');
    } finally {
      setUpdatingApp(false);
    }
  };

  const handleDeleteApplication = async () => {
    try {
      await applicationsApi.delete(id);
      navigate('/applications');
    } catch (err) {
      alert(err.message || 'Failed to delete application.');
    }
  };

  const openCreateInterviewModal = () => {
    setEditingInterviewId(null);
    setInterviewType('TECHNICAL');
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(14, 0, 0, 0);
    setInterviewDate(tomorrow.toISOString().slice(0, 16));
    setInterviewer('');
    setInterviewNotes('');
    setInterviewResult('PENDING');
    setInterviewError('');
    setShowInterviewModal(true);
  };

  const openEditInterviewModal = (interview) => {
    setEditingInterviewId(interview.id);
    setInterviewType(interview.type);
    setInterviewDate(interview.scheduledAt ? interview.scheduledAt.slice(0, 16) : '');
    setInterviewer(interview.interviewer || '');
    setInterviewNotes(interview.notes || '');
    setInterviewResult(interview.result || 'PENDING');
    setInterviewError('');
    setShowInterviewModal(true);
  };

  const handleSaveInterview = async (e) => {
    e.preventDefault();
    setInterviewError('');
    setSavingInterview(true);

    try {
      const payload = {
        type: interviewType,
        scheduledAt: interviewDate,
        interviewer: interviewer.trim() || undefined,
        notes: interviewNotes.trim() || undefined,
        result: interviewResult.trim() || undefined,
      };

      if (editingInterviewId) {
        const updated = await interviewsApi.update(editingInterviewId, payload);
        setInterviews((prev) => prev.map((item) => (item.id === updated.id ? updated : item)));
      } else {
        const created = await interviewsApi.create(id, payload);
        setInterviews((prev) => [...prev, created]);
        // Also refresh application to update interview count if needed
        setApplication((prev) => ({ ...prev, interviewCount: prev.interviewCount + 1 }));
      }
      setShowInterviewModal(false);
    } catch (err) {
      setInterviewError(err.message || 'Failed to save interview.');
    } finally {
      setSavingInterview(false);
    }
  };

  const handleDeleteInterview = async () => {
    if (!interviewToDelete) return;
    try {
      await interviewsApi.delete(interviewToDelete.id);
      setInterviews((prev) => prev.filter((item) => item.id !== interviewToDelete.id));
      setApplication((prev) => ({ ...prev, interviewCount: Math.max(0, prev.interviewCount - 1) }));
      setInterviewToDelete(null);
    } catch (err) {
      alert(err.message || 'Failed to delete interview.');
    }
  };

  const formatSalary = (min, max) => {
    if (!min && !max) return 'Not disclosed';
    if (min && !max) return `$${min.toLocaleString()} / yr`;
    if (!min && max) return `Up to $${max.toLocaleString()} / yr`;
    return `$${min.toLocaleString()} - $${max.toLocaleString()} / yr`;
  };

  if (loading) {
    return <LoadingSpinner message="Loading application details..." />;
  }

  if (error || !application) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
        <AlertCircle size={36} color="#ef4444" style={{ marginBottom: '1rem' }} />
        <h3>Application not found</h3>
        <p style={{ color: 'var(--text-muted)', margin: '0.5rem 0 1.5rem' }}>
          {error || 'The requested application does not exist or you do not have permission to view it.'}
        </p>
        <Link to="/applications" className="btn btn-primary">
          Back to Applications
        </Link>
      </div>
    );
  }

  return (
    <div>
      {/* Back button & top bar */}
      <div className="flex-between mb-md">
        <Link to="/applications" className="nav-link" style={{ display: 'inline-flex' }}>
          <ArrowLeft size={16} /> Back to Applications
        </Link>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button className="btn btn-secondary btn-sm" onClick={openEditAppModal}>
            <Edit2 size={15} /> Edit
          </button>
          <button
            className="btn btn-secondary btn-sm"
            style={{ color: '#ef4444' }}
            onClick={() => setShowDeleteAppModal(true)}
          >
            <Trash2 size={15} /> Delete
          </button>
        </div>
      </div>

      {/* Main Details Card */}
      <div className="card mb-lg">
        <div className="flex-between mb-md" style={{ flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <h1 style={{ fontSize: '1.65rem', fontWeight: 800 }}>{application.jobTitle}</h1>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginTop: '0.25rem', color: 'var(--text-muted)', flexWrap: 'wrap' }}>
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontWeight: 600, color: 'var(--text-main)' }}>
                <Building2 size={16} /> {application.company?.name}
              </span>
              {application.location && (
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                  <MapPin size={15} /> {application.location}
                </span>
              )}
              {application.company?.website && (
                <a
                  href={application.company.website}
                  target="_blank"
                  rel="noreferrer"
                  style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', color: 'var(--primary)' }}
                >
                  <ExternalLink size={14} /> Company Website
                </a>
              )}
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)' }}>Status:</span>
            <select
              className="form-select"
              style={{ fontWeight: 600, width: 'auto' }}
              value={application.status}
              onChange={(e) => handleStatusChange(e.target.value)}
            >
              {STATUS_OPTIONS.map((st) => (
                <option key={st} value={st}>{st.replace('_', ' ')}</option>
              ))}
            </select>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.25rem', padding: '1.25rem 0', borderTop: '1px solid var(--border)', borderBottom: '1px solid var(--border)', margin: '1rem 0' }}>
          <div>
            <div className="stat-label" style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>
              Compensation
            </div>
            <div style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.3rem', marginTop: '0.2rem' }}>
              <DollarSign size={16} color="#059669" />
              {formatSalary(application.salaryMin, application.salaryMax)}
            </div>
          </div>

          <div>
            <div className="stat-label" style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>
              Applied Date
            </div>
            <div style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.3rem', marginTop: '0.2rem' }}>
              <Calendar size={16} color="#2563eb" />
              {application.appliedDate || 'Not specified'}
            </div>
          </div>

          <div>
            <div className="stat-label" style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>
              Deadline
            </div>
            <div style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.3rem', marginTop: '0.2rem' }}>
              <Clock size={16} color="#d97706" />
              {application.deadline || 'No deadline'}
            </div>
          </div>

          <div>
            <div className="stat-label" style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>
              Job Posting
            </div>
            <div style={{ marginTop: '0.2rem' }}>
              {application.jobUrl ? (
                <a
                  href={application.jobUrl}
                  target="_blank"
                  rel="noreferrer"
                  style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', fontWeight: 600 }}
                >
                  View Posting <ExternalLink size={14} />
                </a>
              ) : (
                <span style={{ color: 'var(--text-muted)' }}>No link provided</span>
              )}
            </div>
          </div>
        </div>

        {/* Application Notes */}
        <div>
          <h4 style={{ fontSize: '0.9rem', fontWeight: 700, marginBottom: '0.4rem', color: '#475569' }}>Notes</h4>
          <p style={{ whiteSpace: 'pre-wrap', color: application.notes ? 'var(--text-main)' : 'var(--text-muted)', fontSize: '0.925rem' }}>
            {application.notes || 'No notes added for this job application.'}
          </p>
        </div>
      </div>

      {/* Interviews Section */}
      <div className="card">
        <div className="flex-between mb-md">
          <div>
            <h3 style={{ fontSize: '1.2rem', fontWeight: 700 }}>Interviews & Assessments</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
              Track scheduled rounds, interviewers, and debrief notes.
            </p>
          </div>
          <button className="btn btn-primary btn-sm" onClick={openCreateInterviewModal}>
            <Plus size={16} /> Schedule Interview
          </button>
        </div>

        {interviews.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '2.5rem 1rem', border: '1px dashed var(--border)', borderRadius: 'var(--radius-sm)' }}>
            <Calendar size={32} color="#94a3b8" style={{ marginBottom: '0.5rem' }} />
            <h4 style={{ fontSize: '0.95rem', fontWeight: 600 }}>No interviews scheduled</h4>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', margin: '0.25rem 0 1rem' }}>
              Add upcoming rounds (OA, phone screen, technical, behavioral, or final) to keep track.
            </p>
            <button className="btn btn-secondary btn-sm" onClick={openCreateInterviewModal}>
              <Plus size={14} /> Add First Interview
            </button>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="table">
              <thead>
                <tr>
                  <th>Round Type</th>
                  <th>Scheduled Date & Time</th>
                  <th>Interviewer</th>
                  <th>Notes</th>
                  <th>Outcome / Status</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {interviews.map((iv) => (
                  <tr key={iv.id}>
                    <td>
                      <span style={{ fontWeight: 700, padding: '0.2rem 0.5rem', backgroundColor: '#eef2ff', color: '#4338ca', borderRadius: '4px', fontSize: '0.8rem' }}>
                        {iv.type}
                      </span>
                    </td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontWeight: 500 }}>
                        <Clock size={14} color="#64748b" />
                        {new Date(iv.scheduledAt).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' })}
                      </div>
                    </td>
                    <td>
                      {iv.interviewer ? (
                        <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                          <User size={14} color="#64748b" /> {iv.interviewer}
                        </span>
                      ) : (
                        '—'
                      )}
                    </td>
                    <td style={{ maxWidth: '240px', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                      {iv.notes || '—'}
                    </td>
                    <td>
                      <span style={{ padding: '0.2rem 0.5rem', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600, backgroundColor: iv.result === 'PASSED' ? '#ecfdf5' : iv.result === 'FAILED' ? '#fef2f2' : '#f8fafc', color: iv.result === 'PASSED' ? '#047857' : iv.result === 'FAILED' ? '#b91c1c' : '#475569' }}>
                        {iv.result || 'PENDING'}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: '0.4rem' }}>
                        <button
                          className="btn btn-secondary btn-sm"
                          onClick={() => openEditInterviewModal(iv)}
                          title="Edit interview"
                        >
                          <Edit2 size={13} />
                        </button>
                        <button
                          className="btn btn-secondary btn-sm"
                          style={{ color: '#ef4444' }}
                          onClick={() => setInterviewToDelete(iv)}
                          title="Delete interview"
                        >
                          <Trash2 size={13} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Edit Application Modal */}
      <Modal
        isOpen={showEditAppModal}
        onClose={() => setShowEditAppModal(false)}
        title="Edit Job Application"
      >
        {editAppError && (
          <div className="alert alert-danger">
            <AlertCircle size={18} />
            <span>{editAppError}</span>
          </div>
        )}
        <form onSubmit={handleUpdateApplication}>
          <div className="form-group">
            <label className="form-label">Company</label>
            <select
              className="form-select"
              value={editCompanyId}
              onChange={(e) => setEditCompanyId(e.target.value)}
              required
            >
              {companies.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Job Title *</label>
              <input
                type="text"
                className="form-control"
                value={editJobTitle}
                onChange={(e) => setEditJobTitle(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Location</label>
              <input
                type="text"
                className="form-control"
                value={editLocation}
                onChange={(e) => setEditLocation(e.target.value)}
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Job Posting URL</label>
              <input
                type="url"
                className="form-control"
                value={editJobUrl}
                onChange={(e) => setEditJobUrl(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Status</label>
              <select
                className="form-select"
                value={editStatus}
                onChange={(e) => setEditStatus(e.target.value)}
              >
                {STATUS_OPTIONS.map((st) => (
                  <option key={st} value={st}>{st.replace('_', ' ')}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Salary Min</label>
              <input
                type="number"
                className="form-control"
                value={editSalaryMin}
                onChange={(e) => setEditSalaryMin(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Salary Max</label>
              <input
                type="number"
                className="form-control"
                value={editSalaryMax}
                onChange={(e) => setEditSalaryMax(e.target.value)}
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Applied Date</label>
              <input
                type="date"
                className="form-control"
                value={editAppliedDate}
                onChange={(e) => setEditAppliedDate(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Deadline</label>
              <input
                type="date"
                className="form-control"
                value={editDeadline}
                onChange={(e) => setEditDeadline(e.target.value)}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Notes</label>
            <textarea
              className="form-control"
              value={editNotes}
              onChange={(e) => setEditNotes(e.target.value)}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setShowEditAppModal(false)}
              disabled={updatingApp}
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={updatingApp}>
              {updatingApp ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Schedule / Edit Interview Modal */}
      <Modal
        isOpen={showInterviewModal}
        onClose={() => setShowInterviewModal(false)}
        title={editingInterviewId ? 'Edit Interview Round' : 'Schedule Interview Round'}
      >
        {interviewError && (
          <div className="alert alert-danger">
            <AlertCircle size={18} />
            <span>{interviewError}</span>
          </div>
        )}
        <form onSubmit={handleSaveInterview}>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Round Type *</label>
              <select
                className="form-select"
                value={interviewType}
                onChange={(e) => setInterviewType(e.target.value)}
                required
              >
                {INTERVIEW_TYPES.map((t) => (
                  <option key={t} value={t}>{t}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Date & Time *</label>
              <input
                type="datetime-local"
                className="form-control"
                value={interviewDate}
                onChange={(e) => setInterviewDate(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Interviewer</label>
              <input
                type="text"
                className="form-control"
                placeholder="e.g. Sarah Connor (Hiring Mgr)"
                value={interviewer}
                onChange={(e) => setInterviewer(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Result / Status</label>
              <select
                className="form-select"
                value={interviewResult}
                onChange={(e) => setInterviewResult(e.target.value)}
              >
                <option value="PENDING">PENDING</option>
                <option value="PASSED">PASSED</option>
                <option value="FAILED">FAILED</option>
                <option value="RESCHEDULED">RESCHEDULED</option>
              </select>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Notes & Prep</label>
            <textarea
              className="form-control"
              placeholder="Preparation topics, questions asked, feedback..."
              value={interviewNotes}
              onChange={(e) => setInterviewNotes(e.target.value)}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setShowInterviewModal(false)}
              disabled={savingInterview}
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={savingInterview}>
              {savingInterview ? 'Saving...' : 'Save Interview'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete Application Modal */}
      <Modal
        isOpen={showDeleteAppModal}
        onClose={() => setShowDeleteAppModal(false)}
        title="Delete Job Application"
      >
        <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
          Are you sure you want to delete this application for <strong>{application.jobTitle}</strong>?
          All scheduled interviews and notes will be permanently removed.
        </p>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
          <button className="btn btn-secondary" onClick={() => setShowDeleteAppModal(false)}>
            Cancel
          </button>
          <button className="btn btn-danger" onClick={handleDeleteApplication}>
            Delete Application
          </button>
        </div>
      </Modal>

      {/* Delete Interview Modal */}
      <Modal
        isOpen={!!interviewToDelete}
        onClose={() => setInterviewToDelete(null)}
        title="Delete Interview"
      >
        <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
          Are you sure you want to remove this <strong>{interviewToDelete?.type}</strong> interview round?
        </p>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
          <button className="btn btn-secondary" onClick={() => setInterviewToDelete(null)}>
            Cancel
          </button>
          <button className="btn btn-danger" onClick={handleDeleteInterview}>
            Delete Round
          </button>
        </div>
      </Modal>
    </div>
  );
}

