package com.motointel.app.web;

import com.motointel.app.common.ApiException;
import com.motointel.app.common.ApiResponse;
import com.motointel.app.common.PageMeta;
import com.motointel.app.dto.ImportResultDto;
import com.motointel.app.dto.ListingDto;
import com.motointel.app.imports.CsvContract;
import com.motointel.app.imports.CsvImportService;
import com.motointel.app.market.MarketService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketService marketService;
    private final CsvImportService importService;

    public MarketController(MarketService marketService, CsvImportService importService) {
        this.marketService = marketService;
        this.importService = importService;
    }

    @GetMapping("/listings")
    public ApiResponse<List<ListingDto>> listings(@RequestParam(required = false) String source,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) Long manufacturerId,
                                                  @RequestParam(required = false) String matchStatus,
                                                  @RequestParam(required = false) String q,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        Page<ListingDto> result = marketService.list(source, status, manufacturerId, matchStatus, q, page, size);
        return ApiResponse.ok(result.getContent(), PageMeta.of(result));
    }

    @GetMapping("/listings/{id}")
    public ApiResponse<ListingDto> listing(@PathVariable Long id) {
        return ApiResponse.ok(marketService.get(id));
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImportResultDto> importCsv(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.validation("No CSV file uploaded (field 'file')", List.of());
        }
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            return ApiResponse.ok(importService.importCsv(content));
        } catch (IOException e) {
            throw ApiException.validation("Could not read uploaded file: " + e.getMessage(), List.of());
        }
    }

    @GetMapping("/import/template")
    public ResponseEntity<String> template() {
        String body = CsvContract.HEADER + "\n";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"market_listings_template.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }
}
