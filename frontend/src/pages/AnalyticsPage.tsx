import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  ResponsiveContainer,
  LineChart,
  Line,
  Legend,
} from 'recharts';
import {
  useAnalyticsSummary,
  useAnalyticsPrices,
  useAnalyticsModels,
  useAnalyticsTimeline,
} from '../api/hooks';
import { LoadingBlock } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { StatCard } from '../components/Card';
import { Card } from '../components/Card';
import { EmptyState } from '../components/EmptyState';
import { Money } from '../components/Money';

export function AnalyticsPage() {
  const summary = useAnalyticsSummary();
  const prices = useAnalyticsPrices();
  const models = useAnalyticsModels();
  const timeline = useAnalyticsTimeline('week');

  if (summary.isLoading) return <LoadingBlock />;

  return (
    <>
      <div className="page-header">
        <div className="page-title">Analytics</div>
        <div className="page-subtitle">Market trends, price analysis, and model activity</div>
      </div>
      <div className="page-body">
        {summary.error && <ErrorMessage error={summary.error} title="Failed to load analytics" />}

        {/* Headline numbers */}
        {summary.data && (
          <div className="stat-cards-grid" style={{ marginBottom: 28 }}>
            <StatCard label="Manufacturers" value={summary.data.manufacturerCount ?? 0} />
            <StatCard label="Models" value={summary.data.modelCount ?? 0} />
            <StatCard label="Listings" value={summary.data.listingCount ?? 0} />
            <StatCard label="Active" value={summary.data.activeCount ?? 0} highlight />
            <StatCard label="Matched" value={summary.data.matchedCount ?? 0} />
            <StatCard label="Needs Review" value={summary.data.needsReviewCount ?? 0} />
            <StatCard label="Unmatched" value={summary.data.unmatchedCount ?? 0} />
            {summary.data.avgPrice != null && (
              <div className="stat-card">
                <div className="stat-value" style={{ fontSize: '1.3rem' }}>
                  <Money amount={summary.data.avgPrice} />
                </div>
                <div className="stat-label">Avg Price</div>
              </div>
            )}
            {summary.data.medianPrice != null && (
              <div className="stat-card">
                <div className="stat-value" style={{ fontSize: '1.3rem' }}>
                  <Money amount={summary.data.medianPrice} />
                </div>
                <div className="stat-label">Median Price</div>
              </div>
            )}
          </div>
        )}

        <div className="two-col" style={{ marginBottom: 28 }}>
          {/* Price by model bar chart */}
          <Card title="Average Price by Model (Top 20)">
            {prices.isLoading && <LoadingBlock />}
            {prices.error && <ErrorMessage error={prices.error} />}
            {!prices.isLoading && prices.data && (
              prices.data.length === 0 ? (
                <EmptyState message="No price data available." />
              ) : (
                <div className="chart-container">
                  <ResponsiveContainer width="100%" height={260}>
                    <BarChart
                      data={prices.data.slice(0, 20)}
                      margin={{ left: 10, right: 10, bottom: 60, top: 10 }}
                    >
                      <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
                      <XAxis
                        dataKey="modelName"
                        tick={{ fontSize: 10 }}
                        angle={-40}
                        textAnchor="end"
                        interval={0}
                      />
                      <YAxis tick={{ fontSize: 11 }} />
                      <Tooltip formatter={(v: number) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'EUR', maximumFractionDigits: 0 }).format(v)} />
                      <Bar dataKey="avgPrice" fill="#2563eb" name="Avg Price" radius={[3, 3, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              )
            )}
          </Card>

          {/* Timeline chart */}
          <Card title="Weekly Timeline">
            {timeline.isLoading && <LoadingBlock />}
            {timeline.error && <ErrorMessage error={timeline.error} />}
            {!timeline.isLoading && timeline.data && (
              timeline.data.length === 0 ? (
                <EmptyState message="No timeline data available." />
              ) : (
                <div className="chart-container">
                  <ResponsiveContainer width="100%" height={260}>
                    <LineChart data={timeline.data} margin={{ left: 0, right: 10, top: 10, bottom: 20 }}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
                      <XAxis dataKey="bucket" tick={{ fontSize: 10 }} />
                      <YAxis tick={{ fontSize: 11 }} />
                      <Tooltip />
                      <Legend />
                      <Line type="monotone" dataKey="listed" stroke="#10b981" strokeWidth={2} dot={false} name="Listed" />
                      <Line type="monotone" dataKey="removed" stroke="#ef4444" strokeWidth={2} dot={false} name="Removed" />
                      <Line type="monotone" dataKey="priceDrops" stroke="#f59e0b" strokeWidth={2} dot={false} name="Price Drops" />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              )
            )}
          </Card>
        </div>

        {/* Price table */}
        <section className="section">
          <h2 className="section-title">Price by Model</h2>
          <Card>
            {prices.isLoading && <LoadingBlock />}
            {!prices.isLoading && prices.data && (
              prices.data.length === 0 ? (
                <EmptyState message="No price data." />
              ) : (
                <div className="table-wrapper">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Model</th>
                        <th className="text-right">Count</th>
                        <th className="text-right">Min</th>
                        <th className="text-right">Max</th>
                        <th className="text-right">Avg</th>
                        <th className="text-right">Median</th>
                      </tr>
                    </thead>
                    <tbody>
                      {prices.data.map(row => (
                        <tr key={row.modelId}>
                          <td><strong>{row.modelName}</strong></td>
                          <td className="text-right">{row.count}</td>
                          <td className="text-right"><Money amount={row.minPrice} /></td>
                          <td className="text-right"><Money amount={row.maxPrice} /></td>
                          <td className="text-right"><Money amount={row.avgPrice} /></td>
                          <td className="text-right"><Money amount={row.medianPrice} /></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )
            )}
          </Card>
        </section>

        {/* Model activity table */}
        <section className="section">
          <h2 className="section-title">Model Activity</h2>
          <Card>
            {models.isLoading && <LoadingBlock />}
            {models.error && <ErrorMessage error={models.error} />}
            {!models.isLoading && models.data && (
              models.data.length === 0 ? (
                <EmptyState message="No model data." />
              ) : (
                <div className="table-wrapper">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Model</th>
                        <th className="text-right">Active Listings</th>
                        <th className="text-right">Total Observed</th>
                        <th className="text-right">Active %</th>
                      </tr>
                    </thead>
                    <tbody>
                      {models.data.map(row => (
                        <tr key={row.modelId}>
                          <td><strong>{row.modelName}</strong></td>
                          <td className="text-right">{row.activeCount}</td>
                          <td className="text-right">{row.totalObserved}</td>
                          <td className="text-right text-muted">
                            {row.totalObserved > 0
                              ? `${((row.activeCount / row.totalObserved) * 100).toFixed(1)}%`
                              : '—'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )
            )}
          </Card>
        </section>
      </div>
    </>
  );
}
