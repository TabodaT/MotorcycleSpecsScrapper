import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useListings, useManufacturers } from '../api/hooks';
import { LoadingBlock } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { StatusBadge } from '../components/StatusBadge';
import { Pagination } from '../components/Pagination';
import { EmptyState } from '../components/EmptyState';
import { Money } from '../components/Money';

const PAGE_SIZE = 25;

const LISTING_STATUSES = ['active', 'missing_once', 'removed', 'likely_sold', 'expired', 'unknown', 'ignored'];
const MATCH_STATUSES = ['matched', 'needs_review', 'unmatched', 'manual_match', 'ignored', 'rejected'];

export function ListingsPage() {
  const navigate = useNavigate();
  const [filters, setFilters] = useState<{
    source?: string;
    status?: string;
    manufacturerId?: string;
    matchStatus?: string;
    q?: string;
    page: number;
  }>({ page: 0 });

  const { data: manufacturers } = useManufacturers();
  const {
    data: listingsPage,
    isLoading,
    error,
  } = useListings({
    source: filters.source,
    status: filters.status,
    manufacturerId: filters.manufacturerId,
    matchStatus: filters.matchStatus,
    q: filters.q,
    page: filters.page,
    size: PAGE_SIZE,
  });

  function setFilter(key: string, value: string) {
    setFilters(f => ({ ...f, [key]: value || undefined, page: 0 }));
  }

  return (
    <>
      <div className="page-header">
        <div className="page-title">Market Listings</div>
        <div className="page-subtitle">Browse and filter all scraped market listings</div>
      </div>
      <div className="page-body">
        <div className="filter-bar">
          <div className="form-field">
            <label className="form-label">Search</label>
            <input
              className="form-input"
              placeholder="Title, keyword…"
              value={filters.q ?? ''}
              onChange={e => setFilter('q', e.target.value)}
            />
          </div>
          <div className="form-field">
            <label className="form-label">Source</label>
            <input
              className="form-input"
              placeholder="Source name"
              value={filters.source ?? ''}
              onChange={e => setFilter('source', e.target.value)}
            />
          </div>
          <div className="form-field">
            <label className="form-label">Status</label>
            <select className="form-select" value={filters.status ?? ''} onChange={e => setFilter('status', e.target.value)}>
              <option value="">All</option>
              {LISTING_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
          <div className="form-field">
            <label className="form-label">Manufacturer</label>
            <select className="form-select" value={filters.manufacturerId ?? ''} onChange={e => setFilter('manufacturerId', e.target.value)}>
              <option value="">All</option>
              {manufacturers?.map(m => <option key={m.id} value={m.id}>{m.name}</option>)}
            </select>
          </div>
          <div className="form-field">
            <label className="form-label">Match Status</label>
            <select className="form-select" value={filters.matchStatus ?? ''} onChange={e => setFilter('matchStatus', e.target.value)}>
              <option value="">All</option>
              {MATCH_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
        </div>

        <div className="card">
          {isLoading && <LoadingBlock />}
          {error && <div className="card-body"><ErrorMessage error={error} /></div>}
          {!isLoading && !error && (
            listingsPage?.data.length ? (
              <>
                <div className="table-wrapper">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Title</th>
                        <th>Price</th>
                        <th>Source</th>
                        <th>Status</th>
                        <th>Match</th>
                        <th>Model</th>
                        <th>Location</th>
                        <th>Posted</th>
                      </tr>
                    </thead>
                    <tbody>
                      {listingsPage.data.map(listing => (
                        <tr
                          key={listing.id}
                          className="row-link"
                          onClick={() => navigate(`/listings/${listing.id}`)}
                        >
                          <td>
                            <div className="truncate" title={listing.title}>{listing.title ?? '—'}</div>
                          </td>
                          <td><Money amount={listing.price} currency={listing.currency} /></td>
                          <td className="text-sm">{listing.source ?? '—'}</td>
                          <td>
                            {listing.status ? <StatusBadge value={listing.status} type="listing" /> : '—'}
                          </td>
                          <td>
                            {listing.match?.status ? <StatusBadge value={listing.match.status} type="match" /> : '—'}
                          </td>
                          <td className="text-sm truncate" title={listing.match?.modelName}>
                            {listing.match?.modelName ?? '—'}
                          </td>
                          <td className="text-sm text-muted">{listing.location ?? '—'}</td>
                          <td className="text-sm text-muted">
                            {listing.postedDate ? new Date(listing.postedDate).toLocaleDateString() : '—'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                {listingsPage.meta && (
                  <Pagination
                    meta={listingsPage.meta}
                    onPageChange={p => setFilters(f => ({ ...f, page: p }))}
                  />
                )}
              </>
            ) : (
              <EmptyState message="No listings found matching your filters." />
            )
          )}
        </div>
      </div>
    </>
  );
}
