package com.motointel.app.matching;

import com.motointel.app.domain.ListingModelMatch;
import com.motointel.app.domain.MarketListing;
import com.motointel.app.domain.MatchStatus;
import com.motointel.app.domain.ModelAlias;
import com.motointel.app.domain.MotorcycleModel;
import com.motointel.app.domain.MotorcycleSpec;
import com.motointel.app.dto.CandidateDto;
import com.motointel.app.normalize.Normalizer;
import com.motointel.app.repo.ListingModelMatchRepository;
import com.motointel.app.repo.MarketListingRepository;
import com.motointel.app.repo.MatchCandidateRepository;
import com.motointel.app.repo.ModelAliasRepository;
import com.motointel.app.repo.MotorcycleModelRepository;
import com.motointel.app.repo.MotorcycleSpecRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MatchServiceTest {

    private MotorcycleModelRepository modelRepo;
    private ModelAliasRepository aliasRepo;
    private MotorcycleSpecRepository specRepo;
    private MarketListingRepository listingRepo;
    private ListingModelMatchRepository matchRepo;
    private MatchCandidateRepository candidateRepo;
    private MatchService service;

    private final List<CandidateModel> candidates = List.of(
            new CandidateModel(1L, "Honda CBR600RR", "cbr600rr",
                    List.of("CBR 600 RR", "CBR600RR"), 2003, 2022, List.of(599)));

    @BeforeEach
    void setUp() {
        modelRepo = mock(MotorcycleModelRepository.class);
        aliasRepo = mock(ModelAliasRepository.class);
        specRepo = mock(MotorcycleSpecRepository.class);
        listingRepo = mock(MarketListingRepository.class);
        matchRepo = mock(ListingModelMatchRepository.class);
        candidateRepo = mock(MatchCandidateRepository.class);
        service = new MatchService(modelRepo, aliasRepo, specRepo, listingRepo,
                matchRepo, candidateRepo, new MatchingEngine(new Normalizer()), new Normalizer());
    }

    private MarketListing listing(long id, String title) {
        MarketListing l = new MarketListing();
        l.setId(id);
        l.setTitle(title);
        return l;
    }

    @Test
    void runAndPersistInsertsMatchAndCandidates() {
        MarketListing l = listing(1L, "Honda CBR 600 RR 2008");
        l.setExtractedYear(2008);
        l.setExtractedCapacityCc(BigDecimal.valueOf(599));
        when(matchRepo.findByListingId(1L)).thenReturn(Optional.empty());

        MatchOutcome outcome = service.runAndPersist(l, candidates);

        assertEquals(MatchStatus.MATCHED, outcome.status());
        verify(candidateRepo).deleteByListingId(1L);
        verify(candidateRepo).save(any());

        ArgumentCaptor<ListingModelMatch> captor = ArgumentCaptor.forClass(ListingModelMatch.class);
        verify(matchRepo).save(captor.capture());
        assertEquals(MatchStatus.MATCHED, captor.getValue().getStatus());
        assertEquals(1L, captor.getValue().getModelId());
        assertTrue(!captor.getValue().isManual());
    }

    @Test
    void acceptLocksAsManualMatched() {
        when(listingRepo.findById(1L)).thenReturn(Optional.of(listing(1L, "x")));
        when(modelRepo.findById(5L)).thenReturn(Optional.of(new MotorcycleModel()));
        when(matchRepo.findByListingId(1L)).thenReturn(Optional.empty());

        service.accept(1L, 5L, null);

        ArgumentCaptor<ListingModelMatch> captor = ArgumentCaptor.forClass(ListingModelMatch.class);
        verify(matchRepo).save(captor.capture());
        assertEquals(MatchStatus.MATCHED, captor.getValue().getStatus());
        assertTrue(captor.getValue().isManual());
        assertEquals(5L, captor.getValue().getModelId());
    }

    @Test
    void manualMatchCreatesAliasWhenRequested() {
        MarketListing l = listing(1L, "Fireblade");
        l.setExtractedModelText("Honda Fireblade");
        when(listingRepo.findById(1L)).thenReturn(Optional.of(l));
        when(modelRepo.findById(5L)).thenReturn(Optional.of(new MotorcycleModel()));
        when(matchRepo.findByListingId(1L)).thenReturn(Optional.empty());
        when(aliasRepo.existsByModelIdAndNormalizedAlias(5L, "honda fireblade")).thenReturn(false);

        service.manualMatch(1L, 5L, null, true);

        verify(aliasRepo).save(any(ModelAlias.class));
        ArgumentCaptor<ListingModelMatch> captor = ArgumentCaptor.forClass(ListingModelMatch.class);
        verify(matchRepo).save(captor.capture());
        assertEquals(MatchStatus.MANUAL_MATCH, captor.getValue().getStatus());
        assertTrue(captor.getValue().isManual());
    }

    @Test
    void rejectAndIgnoreAreManual() {
        when(listingRepo.findById(1L)).thenReturn(Optional.of(listing(1L, "x")));
        when(matchRepo.findByListingId(1L)).thenReturn(Optional.empty());

        service.reject(1L);
        service.ignore(1L);

        ArgumentCaptor<ListingModelMatch> captor = ArgumentCaptor.forClass(ListingModelMatch.class);
        verify(matchRepo, org.mockito.Mockito.times(2)).save(captor.capture());
        assertEquals(MatchStatus.REJECTED, captor.getAllValues().get(0).getStatus());
        assertEquals(MatchStatus.IGNORED, captor.getAllValues().get(1).getStatus());
        assertTrue(captor.getAllValues().get(0).isManual());
    }

    @Test
    void candidatesForMapsToDto() {
        com.motointel.app.domain.MatchCandidate c = new com.motointel.app.domain.MatchCandidate();
        c.setModelId(1L);
        c.setConfidence(BigDecimal.valueOf(0.72));
        c.setExplanation("base 0.72");
        c.setRank(1);
        when(candidateRepo.findByListingIdOrderByRankAsc(1L)).thenReturn(List.of(c));
        MotorcycleModel m = new MotorcycleModel();
        m.setId(1L);
        m.setName("Honda CBR600RR");
        when(modelRepo.findAll()).thenReturn(List.of(m));

        List<CandidateDto> dtos = service.candidatesFor(1L);
        assertEquals(1, dtos.size());
        assertEquals("Honda CBR600RR", dtos.get(0).modelName());
    }

    @Test
    void loadCandidatesBuildsFromCatalog() {
        MotorcycleModel m = new MotorcycleModel();
        m.setId(1L);
        m.setName("Honda CBR600RR");
        m.setNormalizedName("cbr600rr");
        m.setProductionStartYear(2003);
        m.setProductionEndYear(2022);
        when(modelRepo.findAll()).thenReturn(List.of(m));
        ModelAlias a = new ModelAlias();
        a.setModelId(1L);
        a.setAlias("CBR 600 RR");
        a.setNormalizedAlias("cbr600rr");
        when(aliasRepo.findAll()).thenReturn(List.of(a));
        MotorcycleSpec s = new MotorcycleSpec();
        s.setModelId(1L);
        s.setEngineCapacityCc(BigDecimal.valueOf(599));
        when(specRepo.findAll()).thenReturn(List.of(s));

        List<CandidateModel> built = service.loadCandidates();
        assertEquals(1, built.size());
        assertEquals("cbr600rr", built.get(0).normalizedName());
        assertTrue(built.get(0).aliases().contains("Honda CBR600RR"));
        assertEquals(List.of(599), built.get(0).capacitiesCc());
    }

    @Test
    void manualMatchOnUnknownModelFails() {
        when(listingRepo.findById(1L)).thenReturn(Optional.of(listing(1L, "x")));
        when(modelRepo.findById(99L)).thenReturn(Optional.empty());
        try {
            service.manualMatch(1L, 99L, null, false);
            org.junit.jupiter.api.Assertions.fail("expected validation error");
        } catch (RuntimeException expected) {
            verify(matchRepo, never()).save(any());
        }
    }
}
