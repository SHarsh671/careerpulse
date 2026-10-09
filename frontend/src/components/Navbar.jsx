import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Briefcase, LayoutDashboard, Building2, User, LogOut } from 'lucide-react';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getInitials = (name) => {
    if (!name) return 'U';
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .substring(0, 2);
  };

  return (
    <header className="navbar">
      <div className="navbar-container">
        <NavLink to="/dashboard" className="brand">
          <Briefcase size={22} />
          <span>CareerPulse</span>
        </NavLink>

        <nav>
          <ul className="nav-links">
            <li>
              <NavLink to="/dashboard" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                <LayoutDashboard size={18} /> Dashboard
              </NavLink>
            </li>
            <li>
              <NavLink to="/applications" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                <Briefcase size={18} /> Applications
              </NavLink>
            </li>
            <li>
              <NavLink to="/companies" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                <Building2 size={18} /> Companies
              </NavLink>
            </li>
            <li>
              <NavLink to="/profile" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                <User size={18} /> Profile
              </NavLink>
            </li>
          </ul>
        </nav>

        <div className="nav-user">
          <div className="user-badge">
            <div className="user-avatar">{getInitials(user?.name)}</div>
            <span style={{ display: 'none', md: 'inline' }}>{user?.name}</span>
          </div>
          <button className="btn btn-secondary btn-sm" onClick={handleLogout} title="Log out">
            <LogOut size={16} /> Logout
          </button>
        </div>
      </div>
    </header>
  );
}

