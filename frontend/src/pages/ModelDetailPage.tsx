import { useParams, Link } from 'react-router-dom';
import { useModel } from '../api/hooks';
import { LoadingBlock } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { Card } from '../components/Card';

function fmt(v: number | undefined, unit = '') {
  if (v == null) return '—';
  return `${v}${unit}`;
}

export function ModelDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { data: model, isLoading, error } = useModel(id ?? '');

  if (isLoading) return <LoadingBlock />;
  if (error || !model) return (
    <div className="page-body">
      <Link to="/catalog" className="back-link">← Back to Catalog</Link>
      <ErrorMessage error={error ?? 'Model not found'} title="Error" />
    </div>
  );

  const spec = model.specs?.[0];

  return (
    <>
      <div className="page-header">
        <div className="page-title">{model.manufacturerName} {model.name}</div>
        <div className="page-subtitle">
          {model.productionStartYear
            ? `${model.productionStartYear} – ${model.productionEndYear ?? 'present'}`
            : 'Production years unknown'}
        </div>
      </div>
      <div className="page-body">
        <Link to="/catalog" className="back-link">← Back to Catalog</Link>

        {/* Images */}
        {model.images && model.images.length > 0 && (
          <section className="section">
            <div className="image-grid">
              {model.images.map(img => {
                const src = img.url ?? (img.storageRef ? `/api${img.storageRef}` : null);
                if (!src) return null;
                return <img key={img.id} src={src} alt={model.name} />;
              })}
            </div>
          </section>
        )}

        <div className="two-col">
          {/* Specs */}
          {spec && (
            <Card title="Specifications">
              <table className="spec-table">
                <tbody>
                  <tr><td>Engine</td><td>{fmt(spec.engineCapacityCc, ' cc')}</td></tr>
                  <tr><td>Power</td><td>{fmt(spec.powerKw, ' kW')}</td></tr>
                  <tr><td>Torque</td><td>{fmt(spec.torqueNm, ' Nm')}</td></tr>
                  <tr><td>Dry Weight</td><td>{fmt(spec.dryWeightKg, ' kg')}</td></tr>
                  <tr><td>Wet Weight</td><td>{fmt(spec.wetWeightKg, ' kg')}</td></tr>
                  <tr><td>Seat Height</td><td>{fmt(spec.seatHeightMm, ' mm')}</td></tr>
                  <tr><td>Fuel Capacity</td><td>{fmt(spec.fuelCapacityL, ' L')}</td></tr>
                  <tr><td>Top Speed</td><td>{fmt(spec.topSpeedKmh, ' km/h')}</td></tr>
                  <tr><td>Cooling</td><td>{spec.cooling ?? '—'}</td></tr>
                  <tr><td>Transmission</td><td>{spec.transmission ?? '—'}</td></tr>
                  <tr><td>Final Drive</td><td>{spec.finalDrive ?? '—'}</td></tr>
                  <tr><td>ABS</td><td>{spec.abs == null ? '—' : spec.abs ? 'Yes' : 'No'}</td></tr>
                  <tr><td>Electric</td><td>{spec.isElectric == null ? '—' : spec.isElectric ? 'Yes' : 'No'}</td></tr>
                </tbody>
              </table>
            </Card>
          )}

          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {/* Variants */}
            {model.variants && model.variants.length > 0 && (
              <Card title={`Variants (${model.variants.length})`}>
                <div className="table-wrapper">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Years</th>
                      </tr>
                    </thead>
                    <tbody>
                      {model.variants.map(v => (
                        <tr key={v.id}>
                          <td>{v.name}</td>
                          <td className="text-muted">{v.yearFrom ?? '?'} – {v.yearTo ?? 'present'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </Card>
            )}

            {/* Aliases */}
            {model.aliases && model.aliases.length > 0 && (
              <Card title="Aliases">
                <div className="tag-list">
                  {model.aliases.map(alias => (
                    <span key={alias} className="tag">{alias}</span>
                  ))}
                </div>
              </Card>
            )}

            {/* Sources */}
            {model.sources && model.sources.length > 0 && (
              <Card title="Data Sources">
                <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '6px' }}>
                  {model.sources.map((src, i) => (
                    <li key={i} style={{ fontSize: '0.83rem' }}>
                      <a href={src.url} target="_blank" rel="noreferrer" className="truncate" style={{ display: 'block' }}>
                        {src.url}
                      </a>
                      {src.fetchedAt && (
                        <span className="text-muted text-sm">
                          Fetched: {new Date(src.fetchedAt).toLocaleDateString()}
                        </span>
                      )}
                    </li>
                  ))}
                </ul>
              </Card>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
