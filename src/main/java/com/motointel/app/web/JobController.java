package com.motointel.app.web;

import com.motointel.app.common.ApiResponse;
import com.motointel.app.common.PageMeta;
import com.motointel.app.dto.JobDto;
import com.motointel.app.jobs.JobService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public ApiResponse<List<JobDto>> list(@RequestParam(required = false) String type,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        Page<JobDto> result = jobService.search(type, status, page, size);
        return ApiResponse.ok(result.getContent(), PageMeta.of(result));
    }

    @GetMapping("/{id}")
    public ApiResponse<JobDto> get(@PathVariable Long id) {
        return ApiResponse.ok(jobService.getJob(id));
    }
}
