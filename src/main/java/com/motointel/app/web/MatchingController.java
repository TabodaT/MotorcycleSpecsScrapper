package com.motointel.app.web;

import com.motointel.app.common.ApiResponse;
import com.motointel.app.common.PageMeta;
import com.motointel.app.dto.CandidateDto;
import com.motointel.app.dto.ListingDto;
import com.motointel.app.market.MarketService;
import com.motointel.app.matching.MatchService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matching")
public class MatchingController {

    private final MatchService matchService;
    private final MarketService marketService;

    public MatchingController(MatchService matchService, MarketService marketService) {
        this.matchService = matchService;
        this.marketService = marketService;
    }

    @GetMapping("/review")
    public ApiResponse<List<ListingDto>> review(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        Page<ListingDto> result = marketService.reviewQueue(page, size);
        return ApiResponse.ok(result.getContent(), PageMeta.of(result));
    }

    @GetMapping("/listings/{id}/candidates")
    public ApiResponse<List<CandidateDto>> candidates(@PathVariable Long id) {
        return ApiResponse.ok(matchService.candidatesFor(id));
    }

    @PostMapping("/listings/{id}/accept")
    public ApiResponse<ListingDto> accept(@PathVariable Long id, @RequestBody AcceptRequest body) {
        matchService.accept(id, body.modelId(), body.variantId());
        return ApiResponse.ok(marketService.get(id));
    }

    @PostMapping("/listings/{id}/reject")
    public ApiResponse<ListingDto> reject(@PathVariable Long id) {
        matchService.reject(id);
        return ApiResponse.ok(marketService.get(id));
    }

    @PostMapping("/listings/{id}/ignore")
    public ApiResponse<ListingDto> ignore(@PathVariable Long id) {
        matchService.ignore(id);
        return ApiResponse.ok(marketService.get(id));
    }

    @PostMapping("/listings/{id}/manual-match")
    public ApiResponse<ListingDto> manualMatch(@PathVariable Long id, @RequestBody ManualMatchRequest body) {
        matchService.manualMatch(id, body.modelId(), body.variantId(),
                body.createAlias() != null && body.createAlias());
        return ApiResponse.ok(marketService.get(id));
    }

    public record AcceptRequest(Long modelId, Long variantId) {
    }

    public record ManualMatchRequest(Long modelId, Long variantId, Boolean createAlias) {
    }
}
