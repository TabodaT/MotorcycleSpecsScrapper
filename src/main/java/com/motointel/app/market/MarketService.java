package com.motointel.app.market;

import com.motointel.app.common.ApiException;
import com.motointel.app.domain.ListingModelMatch;
import com.motointel.app.domain.ListingPriceHistory;
import com.motointel.app.domain.ListingStatusHistory;
import com.motointel.app.domain.MarketListing;
import com.motointel.app.domain.MarketListingImage;
import com.motointel.app.domain.MarketListingSnapshot;
import com.motointel.app.domain.MotorcycleModel;
import com.motointel.app.dto.ImageDto;
import com.motointel.app.dto.ListingDto;
import com.motointel.app.repo.ListingModelMatchRepository;
import com.motointel.app.repo.ListingPriceHistoryRepository;
import com.motointel.app.repo.ListingStatusHistoryRepository;
import com.motointel.app.repo.MarketListingImageRepository;
import com.motointel.app.repo.MarketListingRepository;
import com.motointel.app.repo.MarketListingSnapshotRepository;
import com.motointel.app.repo.MotorcycleModelRepository;
import com.motointel.app.storage.MediaUrls;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MarketService {

    private final MarketListingRepository listingRepo;
    private final ListingModelMatchRepository matchRepo;
    private final ListingPriceHistoryRepository priceRepo;
    private final ListingStatusHistoryRepository statusRepo;
    private final MarketListingImageRepository imageRepo;
    private final MarketListingSnapshotRepository snapshotRepo;
    private final MotorcycleModelRepository modelRepo;

    public MarketService(MarketListingRepository listingRepo, ListingModelMatchRepository matchRepo,
                         ListingPriceHistoryRepository priceRepo, ListingStatusHistoryRepository statusRepo,
                         MarketListingImageRepository imageRepo, MarketListingSnapshotRepository snapshotRepo,
                         MotorcycleModelRepository modelRepo) {
        this.listingRepo = listingRepo;
        this.matchRepo = matchRepo;
        this.priceRepo = priceRepo;
        this.statusRepo = statusRepo;
        this.imageRepo = imageRepo;
        this.snapshotRepo = snapshotRepo;
        this.modelRepo = modelRepo;
    }

    public Page<ListingDto> list(String source, String status, Long manufacturerId,
                                 String matchStatus, String q, int page, int size) {
        Page<MarketListing> listings = listingRepo.search(nullable(source), nullable(status),
                nullable(matchStatus), manufacturerId, nullable(q), PageRequest.of(page, size));
        Map<Long, String> names = modelNames();
        return listings.map(l -> toLightDto(l, names));
    }

    public ListingDto get(Long id) {
        MarketListing l = listingRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("Listing not found: " + id));
        return toFullDto(l, modelNames());
    }

    /** Paginated review queue: listings whose match needs review (§4 /api/matching/review). */
    public Page<ListingDto> reviewQueue(int page, int size) {
        Page<ListingModelMatch> matches = matchRepo.findByStatusOrderByDecidedAtDesc(
                com.motointel.app.domain.MatchStatus.NEEDS_REVIEW, PageRequest.of(page, size));
        Map<Long, String> names = modelNames();
        return matches.map(m -> listingRepo.findById(m.getListingId())
                .map(l -> toFullDto(l, names)).orElse(null));
    }

    public ListingDto toLightDto(MarketListing l, Map<Long, String> modelNames) {
        ListingDto.Match match = matchRepo.findByListingId(l.getId())
                .map(m -> toMatch(m, modelNames)).orElse(null);
        return new ListingDto(l.getId(), l.getSource(), l.getUrl(), l.getTitle(), null,
                l.getPrice(), l.getCurrency(), l.getLocation(), l.getSellerType(), l.getPostedDate(),
                l.getFirstObservedAt(), l.getLastObservedAt(), l.getStatus(),
                null, match, List.of(), List.of(), List.of(), null);
    }

    public ListingDto toFullDto(MarketListing l, Map<Long, String> modelNames) {
        ListingDto.Extracted extracted = new ListingDto.Extracted(l.getExtractedYear(),
                l.getExtractedManufacturer(), l.getExtractedModelText(),
                l.getExtractedCapacityCc(), l.getExtractedMileageKm());
        ListingDto.Match match = matchRepo.findByListingId(l.getId())
                .map(m -> toMatch(m, modelNames)).orElse(null);
        List<ListingDto.PriceHistory> priceHistory = priceRepo.findByListingIdOrderByObservedAtAsc(l.getId())
                .stream().map(this::toPrice).toList();
        List<ListingDto.StatusHistory> statusHistory = statusRepo.findByListingIdOrderByAtAsc(l.getId())
                .stream().map(this::toStatus).toList();
        List<ImageDto> images = imageRepo.findByListingId(l.getId()).stream().map(this::toImage).toList();
        String snapshotRef = l.getLatestSnapshotId() == null ? null
                : snapshotRepo.findById(l.getLatestSnapshotId())
                    .map(MarketListingSnapshot::getStorageRef).orElse(null);

        return new ListingDto(l.getId(), l.getSource(), l.getUrl(), l.getTitle(), l.getDescription(),
                l.getPrice(), l.getCurrency(), l.getLocation(), l.getSellerType(), l.getPostedDate(),
                l.getFirstObservedAt(), l.getLastObservedAt(), l.getStatus(),
                extracted, match, priceHistory, statusHistory, images, snapshotRef);
    }

    private ListingDto.Match toMatch(ListingModelMatch m, Map<Long, String> modelNames) {
        return new ListingDto.Match(m.getStatus(), m.getModelId(),
                m.getModelId() == null ? null : modelNames.get(m.getModelId()),
                m.getVariantId(), m.getConfidence(), m.getExplanation());
    }

    private ListingDto.PriceHistory toPrice(ListingPriceHistory p) {
        return new ListingDto.PriceHistory(p.getPrice(), p.getCurrency(), p.getObservedAt());
    }

    private ListingDto.StatusHistory toStatus(ListingStatusHistory s) {
        return new ListingDto.StatusHistory(s.getStatus(), s.getAt());
    }

    private ImageDto toImage(MarketListingImage img) {
        return new ImageDto(img.getId(), img.getStorageRef(), img.getSourceUrl(),
                MediaUrls.forRef(img.getStorageRef()));
    }

    private Map<Long, String> modelNames() {
        return modelRepo.findAll().stream()
                .collect(Collectors.toMap(MotorcycleModel::getId, MotorcycleModel::getName));
    }

    private static String nullable(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
