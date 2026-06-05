import { useState } from 'react';
import {
  useReviewQueue,
  useMatchCandidates,
  useAcceptMatch,
  useRejectMatch,
  useIgnoreMatch,
  useManualMatch,
  useManufacturers,
  useModels,
} from '../api/hooks';
import { LoadingBlock, LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { StatusBadge } from '../components/StatusBadge';
import { Pagination } from '../components/Pagination';
import { EmptyState } from '../components/EmptyState';
import { Money } from '../components/Money';
import type { ListingDto } from '../api/types';

const PAGE_SIZE = 10;

interface CandidatesPanelProps {
  listing: ListingDto;
  onDone: () => void;
}

function CandidatesPanel({ listing, onDone }: CandidatesPanelProps) {
  const { data: candidates, isLoading, error } = useMatchCandidates(listing.id);
  const acceptMatch = useAcceptMatch();
  const rejectMatch = useRejectMatch();
  const ignoreMatch = useIgnoreMatch();
  const manualMatch = useManualMatch();

  const [showManual, setShowManual] = useState(false);
  const [manualModelId, setManualModelId] = useState('');
  const [manualVariantId, setManualVariantId] = useState('');
  const [createAlias, setCreateAlias] = useState(false);
  const [manualMfrId, setManualMfrId] = useState('');

  const { data: manufacturers } = useManufacturers();
  const { data: modelsPage } = useModels({ manufacturerId: manualMfrId || undefined, size: 100 });

  const isPending =
    acceptMatch.isPending || rejectMatch.isPending || ignoreMatch.isPending || manualMatch.isPending;

  async function handleAccept(modelId: string, variantId?: string) {
    await acceptMatch.mutateAsync({ id: listing.id, body: { modelId, variantId } });
    onDone();
  }

  async function handleReject() {
    await rejectMatch.mutateAsync(listing.id);
    onDone();
  }

  async function handleIgnore() {
    await ignoreMatch.mutateAsync(listing.id);
    onDone();
  }

  async function handleManualMatch() {
    if (!manualModelId) return;
    await manualMatch.mutateAsync({
      id: listing.id,
      body: { modelId: manualModelId, variantId: manualVariantId || undefined, createAlias },
    });
    onDone();
  }

  return (
    <div className="review-card-body">
      {isLoading && <LoadingSpinner size="sm" />}
      {error && <ErrorMessage error={error} />}
      {!isLoading && !error && (
        <>
          {candidates && candidates.length > 0 ? (
            <ul className="candidate-list">
              {candidates.map((c, i) => (
                <li key={c.modelId} className={`candidate-item ${i === 0 ? 'best' : ''}`}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 10 }}>
                    <div>
                      <strong>{c.modelName}</strong>
                      {c.variantId && <span className="text-muted text-sm"> · variant {c.variantId}</span>}
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                      <div className="confidence-bar" style={{ width: 120 }}>
                        <div className="confidence-track">
                          <div
                            className="confidence-fill"
                            style={{ width: `${Math.round((c.confidence ?? 0) * 100)}%` }}
                          />
                        </div>
                        <span className="confidence-label">{((c.confidence ?? 0) * 100).toFixed(0)}%</span>
                      </div>
                      <button
                        className="btn btn-sm btn-success"
                        disabled={isPending}
                        onClick={() => handleAccept(c.modelId, c.variantId)}
                      >
                        Accept
                      </button>
                    </div>
                  </div>
                  {c.explanation && (
                    <p className="text-sm text-muted">{c.explanation}</p>
                  )}
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-muted text-sm" style={{ marginBottom: 10 }}>No automatic candidates found.</p>
          )}

          <div className="btn-group" style={{ marginTop: 14 }}>
            <button className="btn btn-danger btn-sm" disabled={isPending} onClick={handleReject}>
              Reject
            </button>
            <button className="btn btn-secondary btn-sm" disabled={isPending} onClick={handleIgnore}>
              Ignore
            </button>
            <button
              className="btn btn-warning btn-sm"
              disabled={isPending}
              onClick={() => setShowManual(!showManual)}
            >
              Manual Match…
            </button>
          </div>

          {showManual && (
            <div
              style={{
                marginTop: 14,
                padding: 14,
                background: '#f9fafb',
                borderRadius: 8,
                border: '1px solid #e5e7eb',
              }}
            >
              <h4 style={{ fontSize: '0.875rem', fontWeight: 600, marginBottom: 10 }}>Manual Match</h4>
              <div className="form-row">
                <div className="form-field">
                  <label className="form-label">Manufacturer</label>
                  <select
                    className="form-select"
                    value={manualMfrId}
                    onChange={e => { setManualMfrId(e.target.value); setManualModelId(''); }}
                  >
                    <option value="">All</option>
                    {manufacturers?.map(m => <option key={m.id} value={m.id}>{m.name}</option>)}
                  </select>
                </div>
                <div className="form-field">
                  <label className="form-label">Model *</label>
                  <select
                    className="form-select"
                    value={manualModelId}
                    onChange={e => setManualModelId(e.target.value)}
                  >
                    <option value="">Select model…</option>
                    {modelsPage?.data.map(m => (
                      <option key={m.id} value={m.id}>{m.name}</option>
                    ))}
                  </select>
                </div>
                <div className="form-field">
                  <label className="form-label">Variant ID (optional)</label>
                  <input
                    className="form-input"
                    value={manualVariantId}
                    onChange={e => setManualVariantId(e.target.value)}
                    placeholder="Variant ID"
                  />
                </div>
              </div>
              <label style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 10, fontSize: '0.875rem' }}>
                <input
                  type="checkbox"
                  checked={createAlias}
                  onChange={e => setCreateAlias(e.target.checked)}
                />
                Create alias for this title text
              </label>
              <button
                className="btn btn-primary btn-sm"
                disabled={!manualModelId || isPending}
                onClick={handleManualMatch}
              >
                {isPending ? 'Saving…' : 'Save Manual Match'}
              </button>
            </div>
          )}

          {(acceptMatch.error || rejectMatch.error || ignoreMatch.error || manualMatch.error) && (
            <div style={{ marginTop: 10 }}>
              <ErrorMessage error={acceptMatch.error ?? rejectMatch.error ?? ignoreMatch.error ?? manualMatch.error} />
            </div>
          )}
        </>
      )}
    </div>
  );
}

export function MatchReviewPage() {
  const [page, setPage] = useState(0);
  const [expandedId, setExpandedId] = useState<string | null>(null);

  const { data: queue, isLoading, error, refetch } = useReviewQueue({ page, size: PAGE_SIZE });

  if (isLoading) return <LoadingBlock />;

  return (
    <>
      <div className="page-header">
        <div className="page-title">Match Review</div>
        <div className="page-subtitle">
          Review and resolve listings that need manual matching
          {queue && ` — ${queue.meta.total} pending`}
        </div>
      </div>
      <div className="page-body">
        {error && <ErrorMessage error={error} />}
        {!error && queue?.data.length === 0 && (
          <EmptyState message="All listings have been reviewed. Great work!" icon="🎉" />
        )}
        {queue?.data.map(listing => (
          <div key={listing.id} className="review-card">
            <div className="review-card-header">
              <div style={{ flex: 1 }}>
                <div style={{ fontWeight: 600 }}>{listing.title ?? listing.id}</div>
                <div className="text-sm text-muted" style={{ marginTop: 2 }}>
                  {listing.source} ·&nbsp;
                  <Money amount={listing.price} currency={listing.currency} /> ·&nbsp;
                  {listing.location ?? ''}
                </div>
              </div>
              <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                {listing.match?.status && <StatusBadge value={listing.match.status} type="match" />}
                <button
                  className="btn btn-sm btn-secondary"
                  onClick={() => setExpandedId(expandedId === listing.id ? null : listing.id)}
                >
                  {expandedId === listing.id ? 'Hide' : 'Review'}
                </button>
              </div>
            </div>
            {expandedId === listing.id && (
              <CandidatesPanel
                listing={listing}
                onDone={() => {
                  setExpandedId(null);
                  void refetch();
                }}
              />
            )}
          </div>
        ))}

        {queue?.meta && (
          <Pagination meta={queue.meta} onPageChange={setPage} />
        )}
      </div>
    </>
  );
}
