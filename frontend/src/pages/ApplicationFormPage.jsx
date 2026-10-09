import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { applicationsApi } from '../api/applicationsApi';
import { companiesApi } from '../api/companiesApi';
import Modal from '../components/Modal';
import { ArrowLeft, Plus, AlertCircle } from 'lucide-react';

export default function ApplicationFormPage() {
  const navigate = useNavigate();
  const [companies, setCompanies] = useState([]);
  const [loadingCompanies, setLoadingCompanies] = useState(true);

  // Form Fields
  const [companyId, setCompanyId] = useState('');
  const [jobTitle, setJobTitle] = useState('');
  const [location, setLocation] = useState('');
  const [jobUrl, setJobUrl] = useState('');
  const [status, setStatus] = useState('APPLIED');
  const [salaryMin, setSalaryMin] = useState('');
  const [salaryMax, setSalaryMax] = useState('');
  const [appliedDate, setAppliedDate] = useState(new Date().toISOString().split('T')[0]);
  const [deadline, setDeadline] = useState('');
  const [notes, setNotes] = useState('');

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  // Quick Create Company Modal State
  const [showCompanyModal, setShowCompanyModal] = useState(false);
  const [newCompanyName, setNewCompanyName] = useState('');
  const [newCompanyWebsite, setNewCompanyWebsite] = useState('');
  const [newCompanyIndustry, setNewCompanyIndustry] = useState('');
  const [newCompanyLocation, setNewCompanyLocation] = useState('');
  const [creatingCompany, setCreatingCompany] = useState(false);
  const [companyError, setCompanyError] = useState('');

  useEffect(() => {
    loadCompanies();
  }, []);

  const loadCompanies = async () => {
    try {
      setLoadingCompanies(true);
      const data = await companiesApi.getAll();
      setCompanies(data);
      if (data.length > 0 && !companyId) {
        setCompanyId(data[0].id.toString());
      }
    } catch (err) {
      setError('Failed to load companies.');
    } finally {
      setLoadingCompanies(false);
    }
  };

  const handleQuickCreateCompany = async (e) => {
    e.preventDefault();
    if (!newCompanyName.trim()) return;
    setCompanyError('');
    setCreatingCompany(true);

    try {
      const created = await companiesApi.create({
        name: newCompanyName.trim(),
        website: newCompanyWebsite.trim() || undefined,
        industry: newCompanyIndustry.trim() || undefined,
        location: newCompanyLocation.trim() || undefined,
      });

      setCompanies((prev) => [...prev, created]);
      setCompanyId(created.id.toString());
      setShowCompanyModal(false);
      setNewCompanyName('');
      setNewCompanyWebsite('');
      setNewCompanyIndustry('');
      setNewCompanyLocation('');
    } catch (err) {
      setCompanyError(err.message || 'Failed to create company');
    } finally {
      setCreatingCompany(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!companyId) {
      setError('Please select or create a company first.');
      return;
    }

    if (salaryMin && salaryMax && parseInt(salaryMin, 10) > parseInt(salaryMax, 10)) {
      setError('Minimum salary cannot exceed maximum salary.');
      return;
    }

    setSaving(true);
    try {
      const payload = {
        companyId: parseInt(companyId, 10),
        jobTitle: jobTitle.trim(),
        location: location.trim() || undefined,
        jobUrl: jobUrl.trim() || undefined,
        status,
        salaryMin: salaryMin ? parseInt(salaryMin, 10) : undefined,
        salaryMax: salaryMax ? parseInt(salaryMax, 10) : undefined,
        appliedDate: appliedDate || undefined,
        deadline: deadline || undefined,
        notes: notes.trim() || undefined,
      };

      const response = await applicationsApi.create(payload);
      navigate(`/applications/${response.id}`);
    } catch (err) {
      setError(err.message || 'Failed to save application');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div style={{ maxWidth: '780px', margin: '0 auto' }}>
      <div style={{ marginBottom: '1.5rem' }}>
        <Link to="/applications" className="nav-link" style={{ display: 'inline-flex', marginBottom: '0.5rem' }}>
          <ArrowLeft size={16} /> Back to Applications
        </Link>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>Track New Job Application</h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
          Record the details of your role, application timeline, and compensation.
        </p>
      </div>

      <div className="card">
        {error && (
          <div className="alert alert-danger">
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {/* Company Selector */}
          <div className="form-group">
            <label className="form-label">Company *</label>
            <div style={{ display: 'flex', gap: '0.75rem' }}>
              <select
                className="form-select"
                value={companyId}
                onChange={(e) => setCompanyId(e.target.value)}
                required
                disabled={loadingCompanies || companies.length === 0}
              >
                {companies.length === 0 && (
                  <option value="">No companies created yet — click Add New</option>
                )}
                {companies.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setShowCompanyModal(true)}
                style={{ whiteSpace: 'nowrap' }}
              >
                <Plus size={16} /> Add Company
              </button>
            </div>
          </div>

          {/* Job Title & Location */}
          <div className="form-row">
            <div className="form-group">
              <label className="form-label" htmlFor="jobTitle">Job Title *</label>
              <input
                id="jobTitle"
                type="text"
                className="form-control"
                placeholder="e.g. Junior Backend Engineer"
                value={jobTitle}
                onChange={(e) => setJobTitle(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="location">Location</label>
              <input
                id="location"
                type="text"
                className="form-control"
                placeholder="e.g. New York, NY or Remote"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
              />
            </div>
          </div>

          {/* Job URL & Initial Status */}
          <div className="form-row">
            <div className="form-group">
              <label className="form-label" htmlFor="jobUrl">Job Posting URL</label>
              <input
                id="jobUrl"
                type="url"
                className="form-control"
                placeholder="https://..."
                value={jobUrl}
                onChange={(e) => setJobUrl(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Initial Status</label>
              <select
                className="form-select"
                value={status}
                onChange={(e) => setStatus(e.target.value)}
              >
                <option value="SAVED">Saved (Bookmarked)</option>
                <option value="APPLIED">Applied</option>
                <option value="OA">Online Assessment (OA)</option>
                <option value="INTERVIEW">Interview</option>
                <option value="FINAL_INTERVIEW">Final Round</option>
                <option value="OFFER">Offer</option>
                <option value="REJECTED">Rejected</option>
                <option value="WITHDRAWN">Withdrawn</option>
              </select>
            </div>
          </div>

          {/* Salary Min & Max */}
          <div className="form-row">
            <div className="form-group">
              <label className="form-label" htmlFor="salaryMin">Minimum Salary (USD/yr)</label>
              <input
                id="salaryMin"
                type="number"
                className="form-control"
                placeholder="e.g. 90000"
                min="0"
                value={salaryMin}
                onChange={(e) => setSalaryMin(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="salaryMax">Maximum Salary (USD/yr)</label>
              <input
                id="salaryMax"
                type="number"
                className="form-control"
                placeholder="e.g. 120000"
                min="0"
                value={salaryMax}
                onChange={(e) => setSalaryMax(e.target.value)}
              />
            </div>
          </div>

          {/* Dates */}
          <div className="form-row">
            <div className="form-group">
              <label className="form-label" htmlFor="appliedDate">Applied Date</label>
              <input
                id="appliedDate"
                type="date"
                className="form-control"
                value={appliedDate}
                onChange={(e) => setAppliedDate(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="deadline">Application Deadline</label>
              <input
                id="deadline"
                type="date"
                className="form-control"
                value={deadline}
                onChange={(e) => setDeadline(e.target.value)}
              />
            </div>
          </div>

          {/* Notes */}
          <div className="form-group">
            <label className="form-label" htmlFor="notes">Notes & Key Details</label>
            <textarea
              id="notes"
              className="form-control"
              placeholder="Referral contact, tech stack requirements, cover letter notes..."
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
            />
          </div>

          {/* Actions */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
            <Link to="/applications" className="btn btn-secondary">
              Cancel
            </Link>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving Application...' : 'Save Application'}
            </button>
          </div>
        </form>
      </div>

      {/* Quick Add Company Modal */}
      <Modal
        isOpen={showCompanyModal}
        onClose={() => setShowCompanyModal(false)}
        title="Quick Add Company"
      >
        {companyError && (
          <div className="alert alert-danger">
            <AlertCircle size={18} />
            <span>{companyError}</span>
          </div>
        )}
        <form onSubmit={handleQuickCreateCompany}>
          <div className="form-group">
            <label className="form-label">Company Name *</label>
            <input
              type="text"
              className="form-control"
              placeholder="e.g. Stripe, Datadog"
              value={newCompanyName}
              onChange={(e) => setNewCompanyName(e.target.value)}
              required
              autoFocus
            />
          </div>
          <div className="form-group">
            <label className="form-label">Website</label>
            <input
              type="url"
              className="form-control"
              placeholder="https://company.com"
              value={newCompanyWebsite}
              onChange={(e) => setNewCompanyWebsite(e.target.value)}
            />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Industry</label>
              <input
                type="text"
                className="form-control"
                placeholder="Fintech, SaaS"
                value={newCompanyIndustry}
                onChange={(e) => setNewCompanyIndustry(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Headquarters Location</label>
              <input
                type="text"
                className="form-control"
                placeholder="San Francisco, CA"
                value={newCompanyLocation}
                onChange={(e) => setNewCompanyLocation(e.target.value)}
              />
            </div>
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setShowCompanyModal(false)}
              disabled={creatingCompany}
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={creatingCompany}>
              {creatingCompany ? 'Creating...' : 'Create & Select'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}

