import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useManufacturers, useModels } from '../api/hooks';
import { LoadingBlock, LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { Pagination } from '../components/Pagination';
import { EmptyState } from '../components/EmptyState';

const PAGE_SIZE = 20;

export function CatalogPage() {
  const navigate = useNavigate();
  const [selectedMfr, setSelectedMfr] = useState<string>('');
  const [q, setQ] = useState('');
  const [page, setPage] = useState(0);

  const { data: manufacturers, isLoading: mfrLoading } = useManufacturers();
  const {
    data: modelsPage,
    isLoading: modelsLoading,
    error: modelsError,
  } = useModels({ manufacturerId: selectedMfr || undefined, q: q || undefined, page, size: PAGE_SIZE });

  return (
    <>
      <div className="page-header">
        <div className="page-title">Catalog Browser</div>
        <div className="page-subtitle">Browse manufacturers and motorcycle models</div>
      </div>
      <div className="page-body" style={{ display: 'flex', gap: '20px', alignItems: 'flex-start' }}>
        {/* Manufacturer sidebar */}
        <aside style={{ width: 200, minWidth: 200 }}>
          <div className="card">
            <div className="card-header">Manufacturers</div>
            <div style={{ padding: '8px 0' }}>
              {mfrLoading && <div style={{ padding: '12px 16px' }}><LoadingSpinner size="sm" /></div>}
              <button
                className={`nav-link ${!selectedMfr ? 'active' : ''}`}
                style={{ width: '100%', background: !selectedMfr ? '#2563eb' : undefined, color: !selectedMfr ? '#fff' : undefined, border: 'none', textAlign: 'left', cursor: 'pointer' }}
                onClick={() => { setSelectedMfr(''); setPage(0); }}
              >
                All ({manufacturers?.reduce((s, m) => s + m.modelCount, 0) ?? 0})
              </button>
              {manufacturers?.map(m => (
                <button
                  key={m.id}
                  className={`nav-link ${selectedMfr === m.id ? 'active' : ''}`}
                  style={{ width: '100%', border: 'none', textAlign: 'left', cursor: 'pointer', background: selectedMfr === m.id ? '#2563eb' : undefined, color: selectedMfr === m.id ? '#fff' : undefined }}
                  onClick={() => { setSelectedMfr(m.id); setPage(0); }}
                >
                  {m.name} ({m.modelCount})
                </button>
              ))}
            </div>
          </div>
        </aside>

        {/* Models table */}
        <div style={{ flex: 1 }}>
          <div className="filter-bar">
            <div className="form-field">
              <label className="form-label">Search models</label>
              <input
                className="form-input"
                placeholder="Model name…"
                value={q}
                onChange={e => { setQ(e.target.value); setPage(0); }}
              />
            </div>
          </div>

          <div className="card">
            {modelsLoading && <LoadingBlock />}
            {modelsError && <div className="card-body"><ErrorMessage error={modelsError} /></div>}
            {!modelsLoading && !modelsError && (
              <>
                {!modelsPage?.data.length ? (
                  <EmptyState message="No models found." />
                ) : (
                  <div className="table-wrapper">
                    <table className="data-table">
                      <thead>
                        <tr>
                          <th>Model</th>
                          <th>Manufacturer</th>
                          <th>Production</th>
                          <th>Normalized Name</th>
                        </tr>
                      </thead>
                      <tbody>
                        {modelsPage.data.map(model => (
                          <tr
                            key={model.id}
                            className="row-link"
                            onClick={() => navigate(`/catalog/models/${model.id}`)}
                          >
                            <td><strong>{model.name}</strong></td>
                            <td>{model.manufacturerName ?? '—'}</td>
                            <td>
                              {model.productionStartYear
                                ? `${model.productionStartYear}${model.productionEndYear ? ` – ${model.productionEndYear}` : ' – present'}`
                                : '—'}
                            </td>
                            <td className="text-muted text-sm">{model.normalizedName ?? '—'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
                {modelsPage?.meta && (
                  <div style={{ padding: '0 0 4px' }}>
                    <Pagination meta={modelsPage.meta} onPageChange={setPage} />
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
