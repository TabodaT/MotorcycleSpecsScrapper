// ─── Envelope / Meta ────────────────────────────────────────────────────────

export interface Meta {
  total: number;
  page: number;
  size: number;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: ApiError | null;
  meta?: Meta;
}

export interface ApiError {
  code: string;
  message: string;
  details: string[];
}

// ─── Health ─────────────────────────────────────────────────────────────────

export interface HealthDto {
  status: string;
  db: string;
  version: string;
}

// ─── Jobs ────────────────────────────────────────────────────────────────────

export interface JobErrorDto {
  id: string;
  url?: string;
  stage?: string;
  message: string;
  detail?: string;
  createdAt: string;
}

export interface JobDto {
  id: string;
  type: string;
  source?: string;
  status: string;
  startedAt?: string;
  finishedAt?: string;
  counts?: {
    discovered?: number;
    fetched?: number;
    parsed?: number;
    inserted?: number;
    updated?: number;
    failed?: number;
  };
  errorSummary?: string;
  errors?: JobErrorDto[];
}

// ─── Dashboard ───────────────────────────────────────────────────────────────

export interface DashboardSummaryDto {
  manufacturerCount: number;
  modelCount: number;
  listingCount: number;
  activeCount: number;
  removedCount: number;
  likelySoldCount: number;
  unmatchedCount: number;
  needsReviewCount: number;
  latestJobs: JobDto[];
  latestErrors: JobErrorDto[];
}

// ─── Catalog ─────────────────────────────────────────────────────────────────

export interface ManufacturerDto {
  id: string;
  name: string;
  normalizedName: string;
  modelCount: number;
}

export interface ModelVariantDto {
  id: string;
  name: string;
  yearFrom?: number;
  yearTo?: number;
}

export interface ModelSpecDto {
  engineCapacityCc?: number;
  powerKw?: number;
  torqueNm?: number;
  dryWeightKg?: number;
  wetWeightKg?: number;
  seatHeightMm?: number;
  fuelCapacityL?: number;
  topSpeedKmh?: number;
  cooling?: string;
  transmission?: string;
  finalDrive?: string;
  abs?: boolean;
  isElectric?: boolean;
}

export interface ModelImageDto {
  id: string;
  storageRef?: string;
  sourceUrl?: string;
  url?: string;
}

export interface ModelSourceDto {
  url: string;
  fetchedAt?: string;
}

export interface ModelDto {
  id: string;
  manufacturerName?: string;
  name: string;
  normalizedName?: string;
  productionStartYear?: number;
  productionEndYear?: number;
  variants?: ModelVariantDto[];
  specs?: ModelSpecDto[];
  images?: ModelImageDto[];
  aliases?: string[];
  sources?: ModelSourceDto[];
}

// ─── Sources ─────────────────────────────────────────────────────────────────

export interface SourceDto {
  id: string;
  name: string;
  sourceType: string;
  accessMethod: string;
  baseUrl?: string;
  enabled: boolean;
  complianceStatus?: string;
  scheduleCron?: string;
  lastRunAt?: string;
  nextRunAt?: string;
}

// ─── Market Listings ─────────────────────────────────────────────────────────

export type ListingStatus =
  | 'active'
  | 'missing_once'
  | 'removed'
  | 'likely_sold'
  | 'expired'
  | 'unknown'
  | 'ignored';

export type MatchStatus =
  | 'matched'
  | 'needs_review'
  | 'unmatched'
  | 'manual_match'
  | 'ignored'
  | 'rejected';

export interface ListingMatchDto {
  status: MatchStatus;
  modelId?: string;
  modelName?: string;
  variantId?: string;
  confidence?: number;
  explanation?: string;
}

export interface ListingImageDto {
  id: string;
  storageRef?: string;
  sourceUrl?: string;
  url?: string;
}

export interface PriceHistoryDto {
  price: number;
  currency: string;
  observedAt: string;
}

export interface StatusHistoryDto {
  status: string;
  at: string;
}

export interface ListingExtractedDto {
  year?: number;
  manufacturer?: string;
  modelText?: string;
  capacityCc?: number;
  mileageKm?: number;
}

export interface ListingDto {
  id: string;
  source?: string;
  url?: string;
  title?: string;
  description?: string;
  price?: number;
  currency?: string;
  location?: string;
  sellerType?: string;
  postedDate?: string;
  firstObservedAt?: string;
  lastObservedAt?: string;
  status?: ListingStatus;
  extracted?: ListingExtractedDto;
  match?: ListingMatchDto;
  priceHistory?: PriceHistoryDto[];
  statusHistory?: StatusHistoryDto[];
  images?: ListingImageDto[];
  snapshotRef?: string;
}

// ─── Import ──────────────────────────────────────────────────────────────────

export interface RowError {
  row: number;
  field: string;
  message: string;
}

export interface ImportResultDto {
  jobId: string;
  inserted: number;
  updated: number;
  failed: number;
  rowErrors: RowError[];
}

// ─── Matching Candidates ────────────────────────────────────────────────────

export interface MatchCandidateDto {
  modelId: string;
  modelName: string;
  variantId?: string;
  confidence: number;
  explanation?: string;
}

// ─── Analytics ───────────────────────────────────────────────────────────────

export interface AnalyticsSummaryDto {
  manufacturerCount?: number;
  modelCount?: number;
  listingCount?: number;
  activeCount?: number;
  matchedCount?: number;
  needsReviewCount?: number;
  unmatchedCount?: number;
  avgPrice?: number;
  medianPrice?: number;
  [key: string]: number | undefined;
}

export interface PriceByModelDto {
  modelId: string;
  modelName: string;
  count: number;
  minPrice: number;
  maxPrice: number;
  avgPrice: number;
  medianPrice: number;
}

export interface ModelActivityDto {
  modelId: string;
  modelName: string;
  activeCount: number;
  totalObserved: number;
}

export interface TimelineBucketDto {
  bucket: string;
  listed: number;
  removed: number;
  priceDrops: number;
}

// ─── Paginated result wrapper ────────────────────────────────────────────────

export interface Paginated<T> {
  data: T[];
  meta: Meta;
}
