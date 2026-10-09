import React from 'react';
import { FolderOpen } from 'lucide-react';

export default function EmptyState({
  icon: Icon = FolderOpen,
  title = 'No items found',
  description = 'Get started by creating your first record.',
  actionLabel,
  onAction
}) {
  return (
    <div style={{ textAlign: 'center', padding: '3.5rem 1.5rem', backgroundColor: '#ffffff', border: '1px dashed var(--border)', borderRadius: 'var(--radius-md)' }}>
      <div style={{ display: 'inline-flex', padding: '1rem', backgroundColor: '#f1f5f9', borderRadius: '50%', color: 'var(--text-muted)', marginBottom: '1rem' }}>
        <Icon size={32} />
      </div>
      <h4 style={{ fontSize: '1.1rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.35rem' }}>{title}</h4>
      <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', maxWidth: '400px', margin: '0 auto 1.25rem' }}>{description}</p>
      {actionLabel && onAction && (
        <button className="btn btn-primary" onClick={onAction}>
          {actionLabel}
        </button>
      )}
    </div>
  );
}

