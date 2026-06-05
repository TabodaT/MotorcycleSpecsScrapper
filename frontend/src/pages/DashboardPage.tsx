import { useDashboardSummary } from '../api/hooks';
import { StatCard } from '../components/Card';
import { LoadingBlock } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { StatusBadge } from '../components/StatusBadge';
import { EmptyState } from '../components/EmptyState';

function formatDate(d?: string) {
  if (!d) return '—';
  return new Date(d).toLocaleString();
}

export function DashboardPage() {
  const { data, isLoading, error } = useDashboardSummary();

  if (isLoading) return <LoadingBlock />;
  if (error || !data) return (
    <div className="page-body">
      <ErrorMessage error={error ?? 'No data'} title="Failed to load dashboard" />
    </div>
  );

  return (
    <>
      <div className="page-header">
        <div className="page-title">Dashboard</div>
        <div className="page-subtitle">Real-time overview of the motorcycle market database</div>
      </div>
      <div className="page-body">
        <div className="stat-cards-grid">
          <StatCard label="Manufacturers" value={data.manufacturerCount} />
          <StatCard label="Models" value={data.modelCount} />
          <StatCard label="Total Listings" value={data.listingCount} />
          <StatCard label="Active" value={data.activeCount} highlight />
          <StatCard label="Removed" value={data.removedCount} />
          <StatCard label="Likely Sold" value={data.likelySoldCount} />
          <StatCard label="Unmatched" value={data.unmatchedCount} />
          <StatCard label="Needs Review" value={data.needsReviewCount} highlight />
        </div>

        <div className="two-col">
          <section className="section">
            <h2 className="section-title">Latest Jobs</h2>
            <div className="card">
              {!data.latestJobs?.length ? (
                <EmptyState message="No jobs yet." icon="⚙️" />
              ) : (
                <div className="table-wrapper">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>ID</th>
                        <th>Type</th>
                        <th>Source</th>
                        <th>Status</th>
                        <th>Started</th>
                      </tr>
                    </thead>
                    <tbody>
                      {data.latestJobs.map(job => (
                        <tr key={job.id}>
                          <td className="mono truncate" title={String(job.id)}>{job.id}</td>
                          <td>{job.type}</td>
                          <td>{job.source ?? '—'}</td>
                          <td><StatusBadge value={job.status} type="job" /></td>
                          <td className="text-muted text-sm">{formatDate(job.startedAt)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </section>

          <section className="section">
            <h2 className="section-title">Latest Errors</h2>
            <div className="card">
              {!data.latestErrors?.length ? (
                <EmptyState message="No recent errors. All good!" icon="✅" />
              ) : (
                <div className="table-wrapper">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Stage</th>
                        <th>Message</th>
                        <th>At</th>
                      </tr>
                    </thead>
                    <tbody>
                      {data.latestErrors.map(err => (
                        <tr key={err.id}>
                          <td>{err.stage ?? '—'}</td>
                          <td className="truncate" title={err.message}>{err.message}</td>
                          <td className="text-muted text-sm">{formatDate(err.createdAt)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </section>
        </div>
      </div>
    </>
  );
}
