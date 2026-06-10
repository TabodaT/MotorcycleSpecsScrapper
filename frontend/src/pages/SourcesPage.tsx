import { useState } from 'react';
import { useSources, useJobs, usePatchSource, useIngestSource, useJob, useCancelJob } from '../api/hooks';
import { LoadingBlock } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { StatusBadge } from '../components/StatusBadge';
import { Pagination } from '../components/Pagination';
import { EmptyState } from '../components/EmptyState';
import { Card } from '../components/Card';


function StopButton({ jobId, status }: { jobId: string; status: string }) {
  const cancel = useCancelJob();
  if (status !== 'running') return null;
  const pending = cancel.isPending;
  return (
    <button
      className="btn btn-sm btn-danger"
      disabled={pending}
      onClick={(e) => {
        e.stopPropagation();
        cancel.mutate(jobId);
      }}
      title="Stop this running job"
    >
      {pending ? 'Stopping…' : 'Stop'}
    </button>
  );
}

const PAGE_SIZE = 20;

function formatDate(d?: string) {
  if (!d) return '—';
  return new Date(d).toLocaleString();
}

function JobDetailPanel({ jobId, onClose }: { jobId: string; onClose: () => void }) {
  const { data: job, isLoading, error } = useJob(jobId);

  if (isLoading) return <LoadingBlock />;
  if (error || !job) return <ErrorMessage error={error ?? 'Not found'} />;

  return (
    <div className="card" style={{ marginTop: 20 }}>
      <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span>Job Detail: #{job.id}</span>
        <div style={{ display: 'flex', gap: 8 }}>
          <StopButton jobId={job.id} status={job.status} />
          <button className="btn btn-sm btn-secondary" onClick={onClose}>✕ Close</button>
        </div>
      </div>
      <div className="card-body">
        <div className="detail-grid" style={{ marginBottom: 16 }}>
          <span className="detail-label">Type</span><span className="detail-value">{job.type}</span>
          <span className="detail-label">Status</span><span className="detail-value"><StatusBadge value={job.status} type="job" /></span>
          <span className="detail-label">Source</span><span className="detail-value">{job.source ?? '—'}</span>
          <span className="detail-label">Started</span><span className="detail-value">{formatDate(job.startedAt)}</span>
          <span className="detail-label">Finished</span><span className="detail-value">{formatDate(job.finishedAt)}</span>
        </div>

        {job.counts && (
          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 16 }}>
            {Object.entries(job.counts).map(([k, v]) => (
              <div key={k} className="stat-card" style={{ minWidth: 80, padding: '10px 12px' }}>
                <div className="stat-value" style={{ fontSize: '1.2rem' }}>{v ?? 0}</div>
                <div className="stat-label">{k}</div>
              </div>
            ))}
          </div>
        )}

        {job.errorSummary && (
          <div className="error-box" style={{ marginBottom: 12 }}>{job.errorSummary}</div>
        )}

        {job.errors && job.errors.length > 0 && (
          <>
            <h3 style={{ fontSize: '0.9rem', fontWeight: 600, marginBottom: 8 }}>Errors ({job.errors.length})</h3>
            <div className="table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Stage</th>
                    <th>URL</th>
                    <th>Message</th>
                    <th>At</th>
                  </tr>
                </thead>
                <tbody>
                  {job.errors.map(e => (
                    <tr key={e.id}>
                      <td>{e.stage ?? '—'}</td>
                      <td className="truncate text-sm" title={e.url}>{e.url ?? '—'}</td>
                      <td className="truncate" title={e.message}>{e.message}</td>
                      <td className="text-muted text-sm">{formatDate(e.createdAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

export function SourcesPage() {
  const { data: sources, isLoading: srcLoading, error: srcError } = useSources();
  const [jobFilter, setJobFilter] = useState<{ type?: string; status?: string; page: number }>({ page: 0 });
  const { data: jobsPage, isLoading: jobsLoading } = useJobs({
    type: jobFilter.type,
    status: jobFilter.status,
    page: jobFilter.page,
    size: PAGE_SIZE,
  });
  const [selectedJobId, setSelectedJobId] = useState<string | null>(null);
  const patchSource = usePatchSource();
  const ingestSource = useIngestSource();
  const [ingestingId, setIngestingId] = useState<string | null>(null);
  const [ingestResult, setIngestResult] = useState<Record<string, string>>({});

  async function handleIngest(id: string) {
    setIngestingId(id);
    try {
      const res = await ingestSource.mutateAsync(id);
      setIngestResult(prev => ({ ...prev, [id]: `Job started: ${res.jobId}` }));
    } catch (err) {
      setIngestResult(prev => ({ ...prev, [id]: err instanceof Error ? err.message : 'Error' }));
    } finally {
      setIngestingId(null);
    }
  }

  return (
    <>
      <div className="page-header">
        <div className="page-title">Sources & Jobs</div>
        <div className="page-subtitle">Manage data sources and view ingestion jobs</div>
      </div>
      <div className="page-body">
        {/* Sources table */}
        <section className="section">
          <h2 className="section-title">Sources</h2>
          <div className="card">
            {srcLoading && <LoadingBlock />}
            {srcError && <div className="card-body"><ErrorMessage error={srcError} /></div>}
            {!srcLoading && !srcError && (
              sources && sources.length > 0 ? (
                <div className="table-wrapper">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Type</th>
                        <th>Access</th>
                        <th>Schedule</th>
                        <th>Last Run</th>
                        <th>Next Run</th>
                        <th>Status</th>
                        <th>Enabled</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {sources.map(src => (
                        <tr key={src.id}>
                          <td><strong>{src.name}</strong></td>
                          <td>{src.sourceType}</td>
                          <td>{src.accessMethod}</td>
                          <td className="mono text-sm">{src.scheduleCron ?? '—'}</td>
                          <td className="text-muted text-sm">{formatDate(src.lastRunAt)}</td>
                          <td className="text-muted text-sm">{formatDate(src.nextRunAt)}</td>
                          <td>{src.complianceStatus ? <StatusBadge value={src.complianceStatus} /> : '—'}</td>
                          <td>
                            <label className="toggle">
                              <input
                                type="checkbox"
                                checked={src.enabled}
                                onChange={e =>
                                  patchSource.mutate({ id: src.id, body: { enabled: e.target.checked } })
                                }
                              />
                              <span className="toggle-slider" />
                            </label>
                          </td>
                          <td>
                            <button
                              className="btn btn-sm btn-primary"
                              disabled={ingestingId === src.id}
                              onClick={() => handleIngest(src.id)}
                            >
                              {ingestingId === src.id ? 'Starting…' : 'Run Ingest'}
                            </button>
                            {ingestResult[src.id] && (
                              <div className="text-sm text-muted" style={{ marginTop: 4 }}>
                                {ingestResult[src.id]}
                              </div>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <EmptyState message="No sources configured." />
              )
            )}
          </div>
        </section>

        {/* Jobs table */}
        <section className="section">
          <h2 className="section-title">Jobs</h2>
          <div className="filter-bar">
            <div className="form-field">
              <label className="form-label">Type</label>
              <input
                className="form-input"
                placeholder="e.g. INGEST"
                value={jobFilter.type ?? ''}
                onChange={e => setJobFilter(f => ({ ...f, type: e.target.value || undefined, page: 0 }))}
              />
            </div>
            <div className="form-field">
              <label className="form-label">Status</label>
              <select
                className="form-select"
                value={jobFilter.status ?? ''}
                onChange={e => setJobFilter(f => ({ ...f, status: e.target.value || undefined, page: 0 }))}
              >
                <option value="">All</option>
                <option value="RUNNING">RUNNING</option>
                <option value="COMPLETED">COMPLETED</option>
                <option value="FAILED">FAILED</option>
                <option value="PENDING">PENDING</option>
              </select>
            </div>
          </div>
          <Card>
            {jobsLoading && <LoadingBlock />}
            {!jobsLoading && (
              jobsPage?.data.length ? (
                <>
                  <div className="table-wrapper">
                    <table className="data-table">
                      <thead>
                        <tr>
                          <th>ID</th>
                          <th>Type</th>
                          <th>Source</th>
                          <th>Status</th>
                          <th>Started</th>
                          <th>Finished</th>
                          <th>Summary</th>
                          <th>Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {jobsPage.data.map(job => (
                          <tr key={job.id} className="row-link" onClick={() => setSelectedJobId(job.id)}>
                            <td className="mono text-sm">{job.id}</td>
                            <td>{job.type}</td>
                            <td>{job.source ?? '—'}</td>
                            <td><StatusBadge value={job.status} type="job" /></td>
                            <td className="text-muted text-sm">{formatDate(job.startedAt)}</td>
                            <td className="text-muted text-sm">{formatDate(job.finishedAt)}</td>
                            <td className="text-sm truncate" title={job.errorSummary}>{job.errorSummary ?? '—'}</td>
                            <td onClick={(e) => e.stopPropagation()}>
                              <StopButton jobId={job.id} status={job.status} />
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                  {jobsPage.meta && (
                    <Pagination
                      meta={jobsPage.meta}
                      onPageChange={p => setJobFilter(f => ({ ...f, page: p }))}
                    />
                  )}
                </>
              ) : (
                <EmptyState message="No jobs found." />
              )
            )}
          </Card>
        </section>

        {selectedJobId && (
          <JobDetailPanel jobId={selectedJobId} onClose={() => setSelectedJobId(null)} />
        )}
      </div>
    </>
  );
}
