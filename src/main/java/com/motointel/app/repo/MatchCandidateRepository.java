package com.motointel.app.repo;

import com.motointel.app.domain.MatchCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MatchCandidateRepository extends JpaRepository<MatchCandidate, Long> {
    List<MatchCandidate> findByListingIdOrderByRankAsc(Long listingId);

    @Transactional
    void deleteByListingId(Long listingId);
}
