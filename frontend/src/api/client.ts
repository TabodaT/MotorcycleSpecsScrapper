import type {
  ApiResponse,
  HealthDto,
  DashboardSummaryDto,
  ManufacturerDto,
  ModelDto,
  SourceDto,
  JobDto,
  ListingDto,
  ImportResultDto,
  MatchCandidateDto,
  AnalyticsSummaryDto,
  PriceByModelDto,
  ModelActivityDto,
  TimelineBucketDto,
  Paginated,
} from './types';

// ─── Core fetch helper ───────────────────────────────────────────────────────

async function apiFetch<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(path, {
    headers: { 'Content-Type': 'application/json', ...(options?.headers ?? {}) },
    ...options,
  });

  if (!res.ok) {
    // Try to parse error body
    try {
      const body = (await res.json()) as ApiResponse<T>;
      if (!body.success && body.error) {
        throw new Error(body.error.message || `HTTP ${res.status}`);
      }
    } catch {
      // fall through
    }
    throw new Error(`HTTP ${res.status} ${res.statusText}`);
  }

  const body = (await res.json()) as ApiResponse<T>;

  if (!body.success) {
    throw new Error(body.error?.message ?? 'Unknown API error');
  }

  return body.data as T;
}

async function apiFetchPaginated<T>(
  path: string,
  options?: RequestInit
): Promise<Paginated<T>> {
  const res = await fetch(path, {
    headers: { 'Content-Type': 'application/json', ...(options?.headers ?? {}) },
    ...options,
  });

  if (!res.ok) {
    throw new Error(`HTTP ${res.status} ${res.statusText}`);
  }

  const body = (await res.json()) as ApiResponse<T[]>;

  if (!body.success) {
    throw new Error(body.error?.message ?? 'Unknown API error');
  }

  return {
    data: (body.data ?? []) as T[],
    meta: body.meta ?? { total: 0, page: 0, size: 0 },
  };
}

type QueryParams = Record<string, string | number | boolean | undefined>;

function buildQuery(params: QueryParams): string {
  const entries = Object.entries(params).filter(
    ([, v]) => v !== undefined && v !== null && v !== ''
  );
  if (entries.length === 0) return '';
  return '?' + new URLSearchParams(entries.map(([k, v]) => [k, String(v)])).toString();
}

// ─── Health ──────────────────────────────────────────────────────────────────

export async function getHealth(): Promise<HealthDto> {
  const res = await fetch('/api/health');
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json() as Promise<HealthDto>;
}

// ─── Dashboard ───────────────────────────────────────────────────────────────

export async function getDashboardSummary(): Promise<DashboardSummaryDto> {
  return apiFetch<DashboardSummaryDto>('/api/dashboard/summary');
}

// ─── Catalog ─────────────────────────────────────────────────────────────────

export async function getManufacturers(): Promise<ManufacturerDto[]> {
  return apiFetch<ManufacturerDto[]>('/api/catalog/manufacturers');
}

export interface ModelListParams {
  manufacturerId?: string;
  q?: string;
  page?: number;
  size?: number;
  [key: string]: string | number | boolean | undefined;
}

export async function getModels(params: ModelListParams = {}): Promise<Paginated<ModelDto>> {
  return apiFetchPaginated<ModelDto>('/api/catalog/models' + buildQuery(params));
}

export async function getModel(id: string): Promise<ModelDto> {
  return apiFetch<ModelDto>(`/api/catalog/models/${id}`);
}

export async function searchCatalog(q: string): Promise<ModelDto[]> {
  return apiFetch<ModelDto[]>(`/api/catalog/search?q=${encodeURIComponent(q)}`);
}

export async function getAliases(modelId: string): Promise<string[]> {
  return apiFetch<string[]>(`/api/catalog/aliases?modelId=${modelId}`);
}

// ─── Sources ─────────────────────────────────────────────────────────────────

export async function getSources(): Promise<SourceDto[]> {
  return apiFetch<SourceDto[]>('/api/sources');
}

export async function patchSource(
  id: string,
  body: { enabled?: boolean; scheduleCron?: string }
): Promise<SourceDto> {
  return apiFetch<SourceDto>(`/api/sources/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(body),
  });
}

export async function ingestSource(id: string): Promise<{ jobId: string }> {
  return apiFetch<{ jobId: string }>(`/api/sources/${id}/ingest`, { method: 'POST' });
}

// ─── Jobs ────────────────────────────────────────────────────────────────────

export interface JobListParams {
  type?: string;
  status?: string;
  page?: number;
  size?: number;
  [key: string]: string | number | boolean | undefined;
}

export async function getJobs(params: JobListParams = {}): Promise<Paginated<JobDto>> {
  return apiFetchPaginated<JobDto>('/api/jobs' + buildQuery(params));
}

export async function getJob(id: string): Promise<JobDto> {
  return apiFetch<JobDto>(`/api/jobs/${id}`);
}

// ─── Market Listings ─────────────────────────────────────────────────────────

export interface ListingListParams {
  source?: string;
  status?: string;
  manufacturerId?: string;
  matchStatus?: string;
  q?: string;
  page?: number;
  size?: number;
  [key: string]: string | number | boolean | undefined;
}

export async function getListings(
  params: ListingListParams = {}
): Promise<Paginated<ListingDto>> {
  return apiFetchPaginated<ListingDto>('/api/market/listings' + buildQuery(params));
}

export async function getListing(id: string): Promise<ListingDto> {
  return apiFetch<ListingDto>(`/api/market/listings/${id}`);
}

export async function importCsv(file: File): Promise<ImportResultDto> {
  const form = new FormData();
  form.append('file', file);
  const res = await fetch('/api/market/import/csv', { method: 'POST', body: form });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  const body = (await res.json()) as ApiResponse<ImportResultDto>;
  if (!body.success) throw new Error(body.error?.message ?? 'Import failed');
  return body.data as ImportResultDto;
}

// ─── Matching ────────────────────────────────────────────────────────────────

export interface ReviewListParams {
  page?: number;
  size?: number;
  [key: string]: string | number | boolean | undefined;
}

export async function getReviewQueue(
  params: ReviewListParams = {}
): Promise<Paginated<ListingDto>> {
  return apiFetchPaginated<ListingDto>('/api/matching/review' + buildQuery(params));
}

export async function getMatchCandidates(id: string): Promise<MatchCandidateDto[]> {
  return apiFetch<MatchCandidateDto[]>(`/api/matching/listings/${id}/candidates`);
}

export async function acceptMatch(
  id: string,
  body: { modelId: string; variantId?: string }
): Promise<ListingDto> {
  return apiFetch<ListingDto>(`/api/matching/listings/${id}/accept`, {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

export async function rejectMatch(id: string): Promise<ListingDto> {
  return apiFetch<ListingDto>(`/api/matching/listings/${id}/reject`, { method: 'POST' });
}

export async function ignoreMatch(id: string): Promise<ListingDto> {
  return apiFetch<ListingDto>(`/api/matching/listings/${id}/ignore`, { method: 'POST' });
}

export async function manualMatch(
  id: string,
  body: { modelId: string; variantId?: string; createAlias?: boolean }
): Promise<ListingDto> {
  return apiFetch<ListingDto>(`/api/matching/listings/${id}/manual-match`, {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

// ─── Analytics ───────────────────────────────────────────────────────────────

export async function getAnalyticsSummary(): Promise<AnalyticsSummaryDto> {
  return apiFetch<AnalyticsSummaryDto>('/api/analytics/summary');
}

export async function getAnalyticsPrices(modelId?: string): Promise<PriceByModelDto[]> {
  const q = modelId ? `?modelId=${modelId}` : '';
  return apiFetch<PriceByModelDto[]>(`/api/analytics/prices${q}`);
}

export async function getAnalyticsModels(): Promise<ModelActivityDto[]> {
  return apiFetch<ModelActivityDto[]>('/api/analytics/models');
}

export async function getAnalyticsTimeline(bucket = 'week'): Promise<TimelineBucketDto[]> {
  return apiFetch<TimelineBucketDto[]>(`/api/analytics/timeline?bucket=${bucket}`);
}
