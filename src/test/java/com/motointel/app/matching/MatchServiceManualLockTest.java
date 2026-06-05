package com.motointel.app.matching;

import com.motointel.app.domain.ListingModelMatch;
import com.motointel.app.domain.MarketListing;
import com.motointel.app.normalize.Normalizer;
import com.motointel.app.repo.ListingModelMatchRepository;
import com.motointel.app.repo.MarketListingRepository;
import com.motointel.app.repo.MatchCandidateRepository;
import com.motointel.app.repo.ModelAliasRepository;
import com.motointel.app.repo.MotorcycleModelRepository;
import com.motointel.app.repo.MotorcycleSpecRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** A manual (accepted) match must never be overwritten by automatic rematching (§7). */
class MatchServiceManualLockTest {

    @Test
    void manualMatchIsNotOverwrittenByRematch() {
        MotorcycleModelRepository modelRepo = Mockito.mock(MotorcycleModelRepository.class);
        ModelAliasRepository aliasRepo = Mockito.mock(ModelAliasRepository.class);
        MotorcycleSpecRepository specRepo = Mockito.mock(MotorcycleSpecRepository.class);
        MarketListingRepository listingRepo = Mockito.mock(MarketListingRepository.class);
        ListingModelMatchRepository matchRepo = Mockito.mock(ListingModelMatchRepository.class);
        MatchCandidateRepository candidateRepo = Mockito.mock(MatchCandidateRepository.class);
        MatchingEngine engine = new MatchingEngine(new Normalizer());

        MatchService service = new MatchService(modelRepo, aliasRepo, specRepo, listingRepo,
                matchRepo, candidateRepo, engine, new Normalizer());

        MarketListing listing = new MarketListing();
        listing.setId(99L);
        listing.setTitle("Honda CBR600RR");

        ListingModelMatch manual = new ListingModelMatch();
        manual.setListingId(99L);
        manual.setManual(true);
        when(matchRepo.findByListingId(99L)).thenReturn(Optional.of(manual));

        MatchOutcome result = service.runAndPersist(listing, List.of());

        assertNull(result, "rematch should be a no-op for a manual match");
        verify(candidateRepo, never()).deleteByListingId(any());
        verify(matchRepo, never()).save(any());
    }
}
