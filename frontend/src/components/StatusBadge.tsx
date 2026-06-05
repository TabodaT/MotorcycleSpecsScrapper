import type { ListingStatus, MatchStatus } from '../api/types';

const LISTING_COLORS: Record<string, string> = {
  active: 'badge-green',
  missing_once: 'badge-yellow',
  removed: 'badge-red',
  likely_sold: 'badge-orange',
  expired: 'badge-gray',
  unknown: 'badge-gray',
  ignored: 'badge-gray',
};

const MATCH_COLORS: Record<string, string> = {
  matched: 'badge-green',
  needs_review: 'badge-yellow',
  unmatched: 'badge-red',
  manual_match: 'badge-blue',
  ignored: 'badge-gray',
  rejected: 'badge-red',
};

const JOB_COLORS: Record<string, string> = {
  RUNNING: 'badge-blue',
  COMPLETED: 'badge-green',
  FAILED: 'badge-red',
  PENDING: 'badge-yellow',
  CANCELLED: 'badge-gray',
};

interface Props {
  value: string;
  type?: 'listing' | 'match' | 'job' | 'generic';
}

export function StatusBadge({ value, type = 'generic' }: Props) {
  let colorClass = 'badge-gray';

  if (type === 'listing') {
    colorClass = LISTING_COLORS[value as ListingStatus] ?? 'badge-gray';
  } else if (type === 'match') {
    colorClass = MATCH_COLORS[value as MatchStatus] ?? 'badge-gray';
  } else if (type === 'job') {
    colorClass = JOB_COLORS[value] ?? 'badge-gray';
  }

  return (
    <span className={`badge ${colorClass}`}>
      {value.replace(/_/g, ' ')}
    </span>
  );
}
