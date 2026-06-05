package com.motointel.app.imports;

import com.motointel.app.domain.IngestionJob;
import com.motointel.app.domain.MarketListing;
import com.motointel.app.domain.MarketListingSnapshot;
import com.motointel.app.dto.ImportResultDto;
import com.motointel.app.jobs.JobService;
import com.motointel.app.lifecycle.ListingLifecycleService;
import com.motointel.app.matching.MatchService;
import com.motointel.app.normalize.Normalizer;
import com.motointel.app.normalize.UrlNormalizer;
import com.motointel.app.repo.ListingPriceHistoryRepository;
import com.motointel.app.repo.ListingStatusHistoryRepository;
import com.motointel.app.repo.ManufacturerRepository;
import com.motointel.app.repo.MarketListingImageRepository;
import com.motointel.app.repo.MarketListingRepository;
import com.motointel.app.repo.MarketListingSnapshotRepository;
import com.motointel.app.repo.MarketSourceRepository;
import com.motointel.app.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CsvImportServiceTest {

    private MarketListingRepository listingRepo;
    private ListingPriceHistoryRepository priceRepo;
    private MarketListingSnapshotRepository snapshotRepo;
    private MarketListingImageRepository imageRepo;
    private MatchService matchService;
    private JobService jobService;
    private CsvImportService service;

    @BeforeEach
    void setUp() {
        listingRepo = mock(MarketListingRepository.class);
        priceRepo = mock(ListingPriceHistoryRepository.class);
        ListingStatusHistoryRepository statusRepo = mock(ListingStatusHistoryRepository.class);
        snapshotRepo = mock(MarketListingSnapshotRepository.class);
        imageRepo = mock(MarketListingImageRepository.class);
        MarketSourceRepository marketSourceRepo = mock(MarketSourceRepository.class);
        ManufacturerRepository manufacturerRepo = mock(ManufacturerRepository.class);
        ListingLifecycleService lifecycle = mock(ListingLifecycleService.class);
        matchService = mock(MatchService.class);
        jobService = mock(JobService.class);
        StorageService storage = mock(StorageService.class);

        service = new CsvImportService(new CsvListingParser(new UrlNormalizer()), listingRepo, priceRepo,
                statusRepo, snapshotRepo, imageRepo, marketSourceRepo, manufacturerRepo, lifecycle,
                matchService, jobService, new Normalizer(), storage);

        AtomicLong idSeq = new AtomicLong(0);
        when(listingRepo.save(any())).thenAnswer(inv -> {
            MarketListing l = inv.getArgument(0);
            if (l.getId() == null) {
                l.setId(idSeq.incrementAndGet());
            }
            return l;
        });
        when(snapshotRepo.save(any())).thenAnswer(inv -> {
            MarketListingSnapshot s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });
        when(marketSourceRepo.findByName(anyString())).thenReturn(Optional.empty());
        when(manufacturerRepo.findAll()).thenReturn(List.of());
        when(imageRepo.findByListingId(anyLong())).thenReturn(List.of());
        when(matchService.loadCandidates()).thenReturn(List.of());
        when(listingRepo.findBySourceAndExternalId(anyString(), anyString())).thenReturn(Optional.empty());
        when(listingRepo.findBySourceAndNormalizedUrl(anyString(), anyString())).thenReturn(Optional.empty());

        IngestionJob job = new IngestionJob();
        job.setId(100L);
        when(jobService.start(anyString(), any())).thenReturn(job);
        when(jobService.finish(any(), anyString())).thenReturn(job);
    }

    @Test
    void importsValidRowsAndAutoMatches() {
        String csv = CsvContract.HEADER + "\n"
                + "OLX,https://x.test/olx/1,Honda CBR600RR 2008,6500,EUR,2024-05-01,desc,Cluj,2024-04-28,private,a1,20000,\n"
                + "OLX,https://x.test/olx/2,Yamaha MT-07 2019,7000,EUR,2024-05-02,,Iasi,,dealer,a2,9000,\n";

        ImportResultDto result = service.importCsv(csv);

        assertEquals(2, result.inserted());
        assertEquals(0, result.updated());
        assertEquals(0, result.failed());
        assertTrue(result.rowErrors().isEmpty());
        // each new/changed listing is auto-matched
        verify(matchService, times(2)).runAndPersist(any(), any());
        verify(priceRepo, times(2)).save(any());
    }

    @Test
    void reportsRowErrorsForInvalidRows() {
        String csv = CsvContract.HEADER + "\n"
                + "OLX,https://x.test/olx/1,Honda,6500,EUR,2024-05-01,,,,,a1,,\n"   // valid
                + "OLX,https://x.test/olx/2,Bad,,EUR,2024-05-02,,,,,a2,,\n";       // missing price
        ImportResultDto result = service.importCsv(csv);

        assertEquals(1, result.inserted());
        assertEquals(1, result.failed());
        assertTrue(result.rowErrors().stream().anyMatch(e -> e.field().equals("price")));
    }
}
