import React from 'react';

export default function StatCard({ label, value, icon: Icon, color = '#2563eb', bg = '#eff6ff' }) {
  return (
    <div className="stat-card">
      <div className="stat-info">
        <div className="stat-label">{label}</div>
        <div className="stat-value">{value}</div>
      </div>
      <div className="stat-icon" style={{ backgroundColor: bg, color }}>
        {Icon && <Icon size={22} />}
      </div>
    </div>
  );
}

