package com.motointel.app.web;

import com.motointel.app.common.ApiResponse;
import com.motointel.app.dto.IngestStartedDto;
import com.motointel.app.dto.SourceDto;
import com.motointel.app.sources.SourceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sources")
public class SourceController {

    private final SourceService sourceService;

    public SourceController(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    @GetMapping
    public ApiResponse<List<SourceDto>> list() {
        return ApiResponse.ok(sourceService.list());
    }

    @PostMapping("/{id}/ingest")
    public ApiResponse<IngestStartedDto> ingest(@PathVariable long id) {
        return ApiResponse.ok(sourceService.ingest(id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<SourceDto> patch(@PathVariable long id, @RequestBody PatchSourceRequest body) {
        return ApiResponse.ok(sourceService.patch(id, body.enabled(), body.scheduleCron()));
    }

    public record PatchSourceRequest(Boolean enabled, String scheduleCron) {
    }
}
