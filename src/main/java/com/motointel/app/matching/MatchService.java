package com.motointel.app.matching;

import com.motointel.app.common.ApiException;
import com.motointel.app.domain.ListingModelMatch;
import com.motointel.app.domain.MatchCandidate;
import com.motointel.app.domain.MatchStatus;
import com.motointel.app.domain.ModelAlias;
import com.motointel.app.domain.MotorcycleModel;
import com.motointel.app.domain.MotorcycleSpec;
import com.motointel.app.domain.MarketListing;
import com.motointel.app.dto.CandidateDto;
import com.motointel.app.normalize.Normalizer;
import com.motointel.app.repo.ListingModelMatchRepository;
import com.motointel.app.repo.MarketListingRepository;
import com.motointel.app.repo.MatchCandidateRepository;
import com.motointel.app.repo.ModelAliasRepository;
import com.motointel.app.repo.MotorcycleModelRepository;
import com.motointel.app.repo.MotorcycleSpecRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DB-aware matching: builds candidates from the catalog, runs {@link MatchingEngine},
 * and persists the chosen match plus the top-5 candidates. Manual outcomes
 * ({@code is_manual=true}) are never overwritten by automatic rematching (§7).
 */
@Service
public class MatchService {

    private final MotorcycleModelRepository modelRepo;
    private final ModelAliasRepository aliasRepo;
    private final MotorcycleSpecRepository specRepo;
    private final MarketListingRepository listingRepo;
    private final ListingModelMatchRepository matchRepo;
    private final MatchCandidateRepository candidateRepo;
    private final MatchingEngine engine;
    private final Normalizer normalizer;

    public MatchService(MotorcycleModelRepository modelRepo, ModelAliasRepository aliasRepo,
                        MotorcycleSpecRepository specRepo, MarketListingRepository listingRepo,
                        ListingModelMatchRepository matchRepo, MatchCandidateRepository candidateRepo,
                        MatchingEngine engine, Normalizer normalizer) {
        this.modelRepo = modelRepo;
        this.aliasRepo = aliasRepo;
        this.specRepo = specRepo;
        this.listingRepo = listingRepo;
        this.matchRepo = matchRepo;
        this.candidateRepo = candidateRepo;
        this.engine = engine;
        this.normalizer = normalizer;
    }

    /** Build the candidate set once (models + aliases + capacities). */
    public List<CandidateModel> loadCandidates() {
        List<MotorcycleModel> models = modelRepo.findAll();
        Map<Long, List<String>> aliasesByModel = aliasRepo.findAll().stream()
                .collect(Collectors.groupingBy(ModelAlias::getModelId,
                        Collectors.mapping(ModelAlias::getAlias, Collectors.toList())));
        Map<Long, List<Integer>> capByModel = specRepo.findAll().stream()
                .filter(s -> s.getEngineCapacityCc() != null)
                .collect(Collectors.groupingBy(MotorcycleSpec::getModelId,
                        Collectors.mapping(s -> s.getEngineCapacityCc().intValue(), Collectors.toList())));

        List<CandidateModel> candidates = new ArrayList<>();
        for (MotorcycleModel m : models) {
            List<String> aliases = new ArrayList<>(aliasesByModel.getOrDefault(m.getId(), List.of()));
            aliases.add(m.getName()); // the human display name is also a matchable form
            candidates.add(new CandidateModel(m.getId(), m.getName(), m.getNormalizedName(),
                    aliases, m.getProductionStartYear(), m.getProductionEndYear(),
                    capByModel.getOrDefault(m.getId(), List.of())));
        }
        return candidates;
    }

    @Transactional
    public MatchOutcome autoMatch(MarketListing listing) {
        return runAndPersist(listing, loadCandidates());
    }

    /** Run the engine for a listing and persist results, honouring manual locks. */
    @Transactional
    public MatchOutcome runAndPersist(MarketListing listing, List<CandidateModel> candidates) {
        ListingModelMatch existing = matchRepo.findByListingId(listing.getId()).orElse(null);
        if (existing != null && existing.isManual()) {
            return null; // locked — never overwrite a manual decision
        }

        MatchInput input = toInput(listing);
        MatchOutcome outcome = engine.match(input, candidates);

        // Persist top-5 candidates.
        candidateRepo.deleteByListingId(listing.getId());
        int rank = 1;
        for (ScoredCandidate sc : outcome.candidates()) {
            MatchCandidate mc = new MatchCandidate();
            mc.setListingId(listing.getId());
            mc.setModelId(sc.modelId());
            mc.setConfidence(BigDecimal.valueOf(sc.score()));
            mc.setExplanation(sc.explanation());
            mc.setRank(rank++);
            candidateRepo.save(mc);
        }

        ListingModelMatch match = existing != null ? existing : new ListingModelMatch();
        match.setListingId(listing.getId());
        boolean hasModel = !MatchStatus.UNMATCHED.equals(outcome.status()) && outcome.top() != null;
        match.setModelId(hasModel ? outcome.top().modelId() : null);
        match.setVariantId(null);
        match.setStatus(outcome.status());
        match.setConfidence(BigDecimal.valueOf(outcome.topScore()));
        match.setExplanation(outcome.explanation());
        match.setManual(false);
        match.setDecidedAt(Instant.now());
        matchRepo.save(match);
        return outcome;
    }

    @Transactional(readOnly = true)
    public List<CandidateDto> candidatesFor(Long listingId) {
        Map<Long, String> names = modelDisplayNames();
        return candidateRepo.findByListingIdOrderByRankAsc(listingId).stream()
                .map(c -> new CandidateDto(c.getModelId(), names.get(c.getModelId()), c.getVariantId(),
                        c.getConfidence(), c.getExplanation()))
                .toList();
    }

    @Transactional
    public void accept(Long listingId, Long modelId, Long variantId) {
        requireListing(listingId);
        if (modelId == null || modelRepo.findById(modelId).isEmpty()) {
            throw ApiException.validation("Unknown modelId: " + modelId, List.of());
        }
        ListingModelMatch match = matchRepo.findByListingId(listingId).orElseGet(ListingModelMatch::new);
        match.setListingId(listingId);
        match.setModelId(modelId);
        match.setVariantId(variantId);
        match.setStatus(MatchStatus.MATCHED);
        if (match.getConfidence() == null) {
            match.setConfidence(BigDecimal.ONE);
        }
        match.setExplanation("Accepted by reviewer.");
        match.setManual(true);
        match.setDecidedAt(Instant.now());
        matchRepo.save(match);
    }

    @Transactional
    public void manualMatch(Long listingId, Long modelId, Long variantId, boolean createAlias) {
        MarketListing listing = requireListing(listingId);
        MotorcycleModel model = modelRepo.findById(modelId)
                .orElseThrow(() -> ApiException.validation("Unknown modelId: " + modelId, List.of()));
        ListingModelMatch match = matchRepo.findByListingId(listingId).orElseGet(ListingModelMatch::new);
        match.setListingId(listingId);
        match.setModelId(modelId);
        match.setVariantId(variantId);
        match.setStatus(MatchStatus.MANUAL_MATCH);
        match.setConfidence(BigDecimal.ONE);
        match.setExplanation("Manually matched by reviewer.");
        match.setManual(true);
        match.setDecidedAt(Instant.now());
        matchRepo.save(match);

        if (createAlias) {
            String aliasText = listing.getExtractedModelText() != null && !listing.getExtractedModelText().isBlank()
                    ? listing.getExtractedModelText() : listing.getTitle();
            String normalized = normalizer.normalizeText(aliasText);
            if (!normalized.isBlank() && !aliasRepo.existsByModelIdAndNormalizedAlias(modelId, normalized)) {
                ModelAlias alias = new ModelAlias();
                alias.setModelId(modelId);
                alias.setAlias(aliasText);
                alias.setNormalizedAlias(normalized);
                aliasRepo.save(alias);
            }
        }
    }

    @Transactional
    public void reject(Long listingId) {
        requireListing(listingId);
        ListingModelMatch match = matchRepo.findByListingId(listingId).orElseGet(ListingModelMatch::new);
        match.setListingId(listingId);
        match.setModelId(null);
        match.setVariantId(null);
        match.setStatus(MatchStatus.REJECTED);
        match.setExplanation("Rejected by reviewer.");
        match.setManual(true);
        match.setDecidedAt(Instant.now());
        matchRepo.save(match);
    }

    @Transactional
    public void ignore(Long listingId) {
        requireListing(listingId);
        ListingModelMatch match = matchRepo.findByListingId(listingId).orElseGet(ListingModelMatch::new);
        match.setListingId(listingId);
        match.setStatus(MatchStatus.IGNORED);
        match.setExplanation("Ignored by reviewer.");
        match.setManual(true);
        match.setDecidedAt(Instant.now());
        matchRepo.save(match);
    }

    private MarketListing requireListing(Long listingId) {
        return listingRepo.findById(listingId)
                .orElseThrow(() -> ApiException.notFound("Listing not found: " + listingId));
    }

    private MatchInput toInput(MarketListing listing) {
        StringBuilder text = new StringBuilder();
        if (listing.getExtractedManufacturer() != null) text.append(listing.getExtractedManufacturer()).append(' ');
        if (listing.getExtractedModelText() != null) text.append(listing.getExtractedModelText()).append(' ');
        if (listing.getTitle() != null) text.append(listing.getTitle());
        Integer cap = listing.getExtractedCapacityCc() == null ? null : listing.getExtractedCapacityCc().intValue();
        return new MatchInput(text.toString().trim(), listing.getExtractedYear(), cap);
    }

    private Map<Long, String> modelDisplayNames() {
        return modelRepo.findAll().stream()
                .collect(Collectors.toMap(MotorcycleModel::getId, MotorcycleModel::getName));
    }
}
