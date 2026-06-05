import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import * as client from './client';
import type {
  ModelListParams,
  JobListParams,
  ListingListParams,
  ReviewListParams,
} from './client';

// ─── Health ──────────────────────────────────────────────────────────────────

export function useHealth() {
  return useQuery({ queryKey: ['health'], queryFn: client.getHealth, refetchInterval: 30_000 });
}

// ─── Dashboard ───────────────────────────────────────────────────────────────

export function useDashboardSummary() {
  return useQuery({
    queryKey: ['dashboard', 'summary'],
    queryFn: client.getDashboardSummary,
    refetchInterval: 60_000,
  });
}

// ─── Catalog ─────────────────────────────────────────────────────────────────

export function useManufacturers() {
  return useQuery({
    queryKey: ['catalog', 'manufacturers'],
    queryFn: client.getManufacturers,
  });
}

export function useModels(params: ModelListParams = {}) {
  return useQuery({
    queryKey: ['catalog', 'models', params],
    queryFn: () => client.getModels(params),
  });
}

export function useModel(id: string) {
  return useQuery({
    queryKey: ['catalog', 'models', id],
    queryFn: () => client.getModel(id),
    enabled: !!id,
  });
}

export function useCatalogSearch(q: string) {
  return useQuery({
    queryKey: ['catalog', 'search', q],
    queryFn: () => client.searchCatalog(q),
    enabled: q.length > 1,
  });
}

// ─── Sources ─────────────────────────────────────────────────────────────────

export function useSources() {
  return useQuery({ queryKey: ['sources'], queryFn: client.getSources });
}

export function usePatchSource() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: { enabled?: boolean; scheduleCron?: string } }) =>
      client.patchSource(id, body),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['sources'] }),
  });
}

export function useIngestSource() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => client.ingestSource(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['sources'] });
      qc.invalidateQueries({ queryKey: ['jobs'] });
    },
  });
}

// ─── Jobs ────────────────────────────────────────────────────────────────────

export function useJobs(params: JobListParams = {}) {
  return useQuery({
    queryKey: ['jobs', params],
    queryFn: () => client.getJobs(params),
    refetchInterval: 15_000,
  });
}

export function useJob(id: string) {
  return useQuery({
    queryKey: ['jobs', id],
    queryFn: () => client.getJob(id),
    enabled: !!id,
  });
}

// ─── Market Listings ─────────────────────────────────────────────────────────

export function useListings(params: ListingListParams = {}) {
  return useQuery({
    queryKey: ['listings', params],
    queryFn: () => client.getListings(params),
  });
}

export function useListing(id: string) {
  return useQuery({
    queryKey: ['listings', id],
    queryFn: () => client.getListing(id),
    enabled: !!id,
  });
}

export function useImportCsv() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (file: File) => client.importCsv(file),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['listings'] }),
  });
}

// ─── Matching ────────────────────────────────────────────────────────────────

export function useReviewQueue(params: ReviewListParams = {}) {
  return useQuery({
    queryKey: ['matching', 'review', params],
    queryFn: () => client.getReviewQueue(params),
  });
}

export function useMatchCandidates(id: string) {
  return useQuery({
    queryKey: ['matching', 'candidates', id],
    queryFn: () => client.getMatchCandidates(id),
    enabled: !!id,
  });
}

export function useAcceptMatch() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: { modelId: string; variantId?: string } }) =>
      client.acceptMatch(id, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['matching', 'review'] });
      qc.invalidateQueries({ queryKey: ['listings'] });
    },
  });
}

export function useRejectMatch() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => client.rejectMatch(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['matching', 'review'] });
      qc.invalidateQueries({ queryKey: ['listings'] });
    },
  });
}

export function useIgnoreMatch() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => client.ignoreMatch(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['matching', 'review'] });
      qc.invalidateQueries({ queryKey: ['listings'] });
    },
  });
}

export function useManualMatch() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      id,
      body,
    }: {
      id: string;
      body: { modelId: string; variantId?: string; createAlias?: boolean };
    }) => client.manualMatch(id, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['matching', 'review'] });
      qc.invalidateQueries({ queryKey: ['listings'] });
    },
  });
}

// ─── Analytics ───────────────────────────────────────────────────────────────

export function useAnalyticsSummary() {
  return useQuery({
    queryKey: ['analytics', 'summary'],
    queryFn: client.getAnalyticsSummary,
  });
}

export function useAnalyticsPrices(modelId?: string) {
  return useQuery({
    queryKey: ['analytics', 'prices', modelId],
    queryFn: () => client.getAnalyticsPrices(modelId),
  });
}

export function useAnalyticsModels() {
  return useQuery({
    queryKey: ['analytics', 'models'],
    queryFn: client.getAnalyticsModels,
  });
}

export function useAnalyticsTimeline(bucket = 'week') {
  return useQuery({
    queryKey: ['analytics', 'timeline', bucket],
    queryFn: () => client.getAnalyticsTimeline(bucket),
  });
}
