import React, { useState, useEffect } from 'react';
import { companiesApi } from '../api/companiesApi';
import LoadingSpinner from '../components/LoadingSpinner';
import EmptyState from '../components/EmptyState';
import Modal from '../components/Modal';
import {
  Building2,
  Plus,
  ExternalLink,
  MapPin,
  Briefcase,
  Edit2,
  Trash2,
  AlertCircle
} from 'lucide-react';

export default function CompaniesPage() {
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Add / Edit Modal State
  const [showModal, setShowModal] = useState(false);
  const [editingCompanyId, setEditingCompanyId] = useState(null);
  const [name, setName] = useState('');
  const [website, setWebsite] = useState('');
  const [industry, setIndustry] = useState('');
  const [location, setLocation] = useState('');
  const [notes, setNotes] = useState('');
  const [saving, setSaving] = useState(false);
  const [modalError, setModalError] = useState('');

  // Delete Modal State
  const [companyToDelete, setCompanyToDelete] = useState(null);
  const [deleteError, setDeleteError] = useState('');
  const [deleting, setDeleting] = useState(false);

  const fetchCompanies = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await companiesApi.getAll();
      setCompanies(data);
    } catch (err) {
      setError(err.message || 'Failed to load companies.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCompanies();
  }, []);

  const openCreateModal = () => {
    setEditingCompanyId(null);
    setName('');
    setWebsite('');
    setIndustry('');
    setLocation('');
    setNotes('');
    setModalError('');
    setShowModal(true);
  };

  const openEditModal = (c) => {
    setEditingCompanyId(c.id);
    setName(c.name || '');
    setWebsite(c.website || '');
    setIndustry(c.industry || '');
    setLocation(c.location || '');
    setNotes(c.notes || '');
    setModalError('');
    setShowModal(true);
  };

  const handleSaveCompany = async (e) => {
    e.preventDefault();
    setModalError('');
    setSaving(true);

    try {
      const payload = {
        name: name.trim(),
        website: website.trim() || undefined,
        industry: industry.trim() || undefined,
        location: location.trim() || undefined,
        notes: notes.trim() || undefined,
      };

      if (editingCompanyId) {
        const updated = await companiesApi.update(editingCompanyId, payload);
        setCompanies((prev) => prev.map((item) => (item.id === updated.id ? updated : item)));
      } else {
        const created = await companiesApi.create(payload);
        setCompanies((prev) => [...prev, created]);
      }
      setShowModal(false);
    } catch (err) {
      setModalError(err.message || 'Failed to save company.');
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteCompany = async () => {
    if (!companyToDelete) return;
    setDeleteError('');
    setDeleting(true);

    try {
      await companiesApi.delete(companyToDelete.id);
      setCompanies((prev) => prev.filter((item) => item.id !== companyToDelete.id));
      setCompanyToDelete(null);
    } catch (err) {
      setDeleteError(err.message || 'Failed to delete company.');
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div>
      {/* Header */}
      <div className="flex-between mb-lg" style={{ flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>Target Companies</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
            Keep track of companies you want to work for, their industries, and application history.
          </p>
        </div>
        <button className="btn btn-primary" onClick={openCreateModal}>
          <Plus size={18} /> Add Company
        </button>
      </div>

      {error && (
        <div className="alert alert-danger">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {loading ? (
        <LoadingSpinner message="Loading your companies..." />
      ) : companies.length === 0 ? (
        <EmptyState
          icon={Building2}
          title="No companies added yet"
          description="Create company profiles to organize your job applications and research."
          actionLabel="Add Your First Company"
          onAction={openCreateModal}
        />
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '1.25rem' }}>
          {companies.map((c) => (
            <div key={c.id} className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
              <div>
                <div className="flex-between mb-sm">
                  <h3 style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--text-main)' }}>
                    {c.name}
                  </h3>
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.75rem', fontWeight: 600, padding: '0.2rem 0.55rem', backgroundColor: '#eff6ff', color: '#1d4ed8', borderRadius: '9999px' }}>
                    <Briefcase size={12} /> {c.applicationCount} {c.applicationCount === 1 ? 'app' : 'apps'}
                  </span>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem', fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '0.85rem' }}>
                  {c.industry && (
                    <div>
                      <strong style={{ color: 'var(--text-main)' }}>Industry:</strong> {c.industry}
                    </div>
                  )}
                  {c.location && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                      <MapPin size={14} /> {c.location}
                    </div>
                  )}
                  {c.website && (
                    <div>
                      <a
                        href={c.website}
                        target="_blank"
                        rel="noreferrer"
                        style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', color: 'var(--primary)' }}
                      >
                        <ExternalLink size={14} /> Visit Website
                      </a>
                    </div>
                  )}
                </div>

                {c.notes && (
                  <p style={{ fontSize: '0.85rem', color: '#475569', backgroundColor: '#f8fafc', padding: '0.65rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
                    {c.notes}
                  </p>
                )}
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem', paddingTop: '0.75rem', borderTop: '1px solid var(--border)' }}>
                <button
                  className="btn btn-secondary btn-sm"
                  onClick={() => openEditModal(c)}
                  title="Edit Company"
                >
                  <Edit2 size={14} /> Edit
                </button>
                <button
                  className="btn btn-secondary btn-sm"
                  style={{ color: '#ef4444' }}
                  onClick={() => {
                    setDeleteError('');
                    setCompanyToDelete(c);
                  }}
                  title="Delete Company"
                >
                  <Trash2 size={14} /> Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add / Edit Company Modal */}
      <Modal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        title={editingCompanyId ? 'Edit Company' : 'Add New Company'}
      >
        {modalError && (
          <div className="alert alert-danger">
            <AlertCircle size={18} />
            <span>{modalError}</span>
          </div>
        )}
        <form onSubmit={handleSaveCompany}>
          <div className="form-group">
            <label className="form-label">Company Name *</label>
            <input
              type="text"
              className="form-control"
              placeholder="e.g. Netflix, Snowflake"
              value={name}
              onChange={(e) => setName(e.target.value)}
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
              value={website}
              onChange={(e) => setWebsite(e.target.value)}
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Industry</label>
              <input
                type="text"
                className="form-control"
                placeholder="Cloud Infrastructure, AI"
                value={industry}
                onChange={(e) => setIndustry(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Location / HQ</label>
              <input
                type="text"
                className="form-control"
                placeholder="San Jose, CA"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Notes & Observations</label>
            <textarea
              className="form-control"
              placeholder="Culture, recruiter contacts, engineering blog insights..."
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setShowModal(false)}
              disabled={saving}
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving...' : 'Save Company'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={!!companyToDelete}
        onClose={() => setCompanyToDelete(null)}
        title="Delete Company"
      >
        {deleteError && (
          <div className="alert alert-danger">
            <AlertCircle size={18} />
            <span>{deleteError}</span>
          </div>
        )}
        <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
          Are you sure you want to delete <strong>{companyToDelete?.name}</strong>?
          {companyToDelete?.applicationCount > 0 && (
            <span style={{ display: 'block', marginTop: '0.5rem', color: '#b91c1c', fontWeight: 600 }}>
              Note: This company has {companyToDelete.applicationCount} associated job applications. Companies with existing applications cannot be deleted until those applications are removed.
            </span>
          )}
        </p>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
          <button
            className="btn btn-secondary"
            onClick={() => setCompanyToDelete(null)}
            disabled={deleting}
          >
            Cancel
          </button>
          <button
            className="btn btn-danger"
            onClick={handleDeleteCompany}
            disabled={deleting}
          >
            {deleting ? 'Deleting...' : 'Delete Company'}
          </button>
        </div>
      </Modal>
    </div>
  );
}

