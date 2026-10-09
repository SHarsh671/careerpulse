import React from 'react';

const STATUS_LABELS = {
  SAVED: 'Saved',
  APPLIED: 'Applied',
  OA: 'Online Assessment',
  INTERVIEW: 'Interview',
  FINAL_INTERVIEW: 'Final Interview',
  OFFER: 'Offer Received',
  REJECTED: 'Rejected',
  WITHDRAWN: 'Withdrawn'
};

export default function StatusBadge({ status }) {
  const label = STATUS_LABELS[status] || status;
  return (
    <span className={`status-badge ${status}`}>
      {label}
    </span>
  );
}

