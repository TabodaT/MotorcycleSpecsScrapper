import { NavLink, Routes, Route } from 'react-router-dom';
import { useHealth } from './api/hooks';

// Pages
import { DashboardPage } from './pages/DashboardPage';
import { CatalogPage } from './pages/CatalogPage';
import { ModelDetailPage } from './pages/ModelDetailPage';
import { SourcesPage } from './pages/SourcesPage';
import { ImportPage } from './pages/ImportPage';
import { ListingsPage } from './pages/ListingsPage';
import { ListingDetailPage } from './pages/ListingDetailPage';
import { MatchReviewPage } from './pages/MatchReviewPage';
import { AnalyticsPage } from './pages/AnalyticsPage';

function Sidebar() {
  const { data: health } = useHealth();
  const isUp = health?.status === 'UP';

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <h1>MotoMarket</h1>
        <div className="logo-sub">Intelligence Dashboard</div>
      </div>

      <nav className="sidebar-nav">
        <div className="nav-group-label">Overview</div>
        <NavLink to="/" end className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <i className="nav-icon">⬡</i> Dashboard
        </NavLink>

        <div className="nav-group-label">Data</div>
        <NavLink to="/catalog" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <i className="nav-icon">📋</i> Catalog
        </NavLink>
        <NavLink to="/listings" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <i className="nav-icon">🏷️</i> Listings
        </NavLink>

        <div className="nav-group-label">Operations</div>
        <NavLink to="/sources" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <i className="nav-icon">🔗</i> Sources & Jobs
        </NavLink>
        <NavLink to="/import" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <i className="nav-icon">📥</i> CSV Import
        </NavLink>
        <NavLink to="/match-review" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <i className="nav-icon">🔍</i> Match Review
        </NavLink>

        <div className="nav-group-label">Insights</div>
        <NavLink to="/analytics" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <i className="nav-icon">📊</i> Analytics
        </NavLink>
      </nav>

      <div className="sidebar-footer">
        <span className={`health-dot ${isUp ? 'up' : 'down'}`} />
        {health ? `API ${health.status} · v${health.version}` : 'Connecting…'}
      </div>
    </aside>
  );
}

export default function App() {
  return (
    <div className="app-layout">
      <Sidebar />
      <main className="main-content">
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/catalog" element={<CatalogPage />} />
          <Route path="/catalog/models/:id" element={<ModelDetailPage />} />
          <Route path="/sources" element={<SourcesPage />} />
          <Route path="/import" element={<ImportPage />} />
          <Route path="/listings" element={<ListingsPage />} />
          <Route path="/listings/:id" element={<ListingDetailPage />} />
          <Route path="/match-review" element={<MatchReviewPage />} />
          <Route path="/analytics" element={<AnalyticsPage />} />
        </Routes>
      </main>
    </div>
  );
}
