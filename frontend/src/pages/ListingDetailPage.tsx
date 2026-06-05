import { useParams, Link } from 'react-router-dom';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  ResponsiveContainer,
} from 'recharts';
import { useListing } from '../api/hooks';
import { LoadingBlock } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { StatusBadge } from '../components/StatusBadge';
import { Card } from '../components/Card';
import { Money } from '../components/Money';

function formatDate(d?: string) {
  if (!d) return '—';
  return new Date(d).toLocaleString();
}

export function ListingDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { data: listing, isLoading, error } = useListing(id ?? '');

  if (isLoading) return <LoadingBlock />;
  if (error || !listing) return (
    <div className="page-body">
      <Link to="/listings" className="back-link">← Back to Listings</Link>
      <ErrorMessage error={error ?? 'Listing not found'} title="Error" />
    </div>
  );

  const priceChartData = listing.priceHistory?.map(p => ({
    date: new Date(p.observedAt).toLocaleDateString(),
    price: p.price,
  })) ?? [];

  return (
    <>
      <div className="page-header">
        <div className="page-title">{listing.title ?? 'Listing Detail'}</div>
        <div className="page-subtitle">
          {listing.source} · {listing.location ?? 'Unknown location'}
        </div>
      </div>
      <div className="page-body">
        <Link to="/listings" className="back-link">← Back to Listings</Link>

        {listing.images && listing.images.length > 0 && (
          <section className="section">
            <div className="image-grid">
              {listing.images.map(img => {
                const src = img.url ?? (img.storageRef ? `/api${img.storageRef}` : null);
                if (!src) return null;
                return <img key={img.id} src={src} alt="listing" />;
              })}
            </div>
          </section>
        )}

        <div className="two-col">
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <Card title="Listing Info">
              <div className="detail-grid">
                <span className="detail-label">Price</span>
                <span className="detail-value"><Money amount={listing.price} currency={listing.currency} /></span>
                <span className="detail-label">Status</span>
                <span className="detail-value">
                  {listing.status ? <StatusBadge value={listing.status} type="listing" /> : '—'}
                </span>
                <span className="detail-label">Source</span>
                <span className="detail-value">{listing.source ?? '—'}</span>
                <span className="detail-label">Seller</span>
                <span className="detail-value">{listing.sellerType ?? '—'}</span>
                <span className="detail-label">Location</span>
                <span className="detail-value">{listing.location ?? '—'}</span>
                <span className="detail-label">Posted</span>
                <span className="detail-value">{formatDate(listing.postedDate)}</span>
                <span className="detail-label">First seen</span>
                <span className="detail-value">{formatDate(listing.firstObservedAt)}</span>
                <span className="detail-label">Last seen</span>
                <span className="detail-value">{formatDate(listing.lastObservedAt)}</span>
                {listing.url && (
                  <>
                    <span className="detail-label">URL</span>
                    <span className="detail-value">
                      <a href={listing.url} target="_blank" rel="noreferrer" className="truncate" style={{ display: 'block' }}>
                        {listing.url}
                      </a>
                    </span>
                  </>
                )}
              </div>
            </Card>

            <Card title="Match Info">
              <div className="detail-grid">
                <span className="detail-label">Match Status</span>
                <span className="detail-value">
                  {listing.match?.status ? <StatusBadge value={listing.match.status} type="match" /> : '—'}
                </span>
                <span className="detail-label">Model</span>
                <span className="detail-value">{listing.match?.modelName ?? '—'}</span>
                <span className="detail-label">Confidence</span>
                <span className="detail-value">
                  {listing.match?.confidence != null
                    ? `${(listing.match.confidence * 100).toFixed(0)}%`
                    : '—'}
                </span>
                <span className="detail-label">Explanation</span>
                <span className="detail-value text-sm">{listing.match?.explanation ?? '—'}</span>
              </div>
            </Card>

            {listing.extracted && (
              <Card title="Extracted Data">
                <div className="detail-grid">
                  <span className="detail-label">Year</span>
                  <span className="detail-value">{listing.extracted.year ?? '—'}</span>
                  <span className="detail-label">Manufacturer</span>
                  <span className="detail-value">{listing.extracted.manufacturer ?? '—'}</span>
                  <span className="detail-label">Model Text</span>
                  <span className="detail-value">{listing.extracted.modelText ?? '—'}</span>
                  <span className="detail-label">Capacity</span>
                  <span className="detail-value">
                    {listing.extracted.capacityCc != null ? `${listing.extracted.capacityCc} cc` : '—'}
                  </span>
                  <span className="detail-label">Mileage</span>
                  <span className="detail-value">
                    {listing.extracted.mileageKm != null ? `${listing.extracted.mileageKm.toLocaleString()} km` : '—'}
                  </span>
                </div>
              </Card>
            )}
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            {priceChartData.length > 1 && (
              <Card title="Price History">
                <div className="chart-container">
                  <ResponsiveContainer width="100%" height={180}>
                    <LineChart data={priceChartData}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
                      <XAxis dataKey="date" tick={{ fontSize: 11 }} />
                      <YAxis tick={{ fontSize: 11 }} />
                      <Tooltip />
                      <Line type="monotone" dataKey="price" stroke="#2563eb" dot={false} strokeWidth={2} />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </Card>
            )}

            {listing.statusHistory && listing.statusHistory.length > 0 && (
              <Card title="Status History">
                <div className="table-wrapper">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Status</th>
                        <th>At</th>
                      </tr>
                    </thead>
                    <tbody>
                      {listing.statusHistory.map((s, i) => (
                        <tr key={i}>
                          <td><StatusBadge value={s.status} type="listing" /></td>
                          <td className="text-muted text-sm">{formatDate(s.at)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </Card>
            )}

            {listing.description && (
              <Card title="Description">
                <p style={{ fontSize: '0.875rem', lineHeight: 1.6, color: '#374151' }}>
                  {listing.description}
                </p>
              </Card>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
