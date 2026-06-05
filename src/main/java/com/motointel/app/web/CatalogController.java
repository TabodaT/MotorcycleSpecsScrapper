package com.motointel.app.web;

import com.motointel.app.catalog.CatalogService;
import com.motointel.app.common.ApiResponse;
import com.motointel.app.common.PageMeta;
import com.motointel.app.dto.ManufacturerDto;
import com.motointel.app.dto.ModelDto;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/manufacturers")
    public ApiResponse<List<ManufacturerDto>> manufacturers() {
        return ApiResponse.ok(catalogService.listManufacturers());
    }

    @GetMapping("/models")
    public ApiResponse<List<ModelDto>> models(@RequestParam(required = false) Long manufacturerId,
                                              @RequestParam(required = false) String q,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        Page<ModelDto> result = catalogService.listModels(manufacturerId, q, page, size);
        return ApiResponse.ok(result.getContent(), PageMeta.of(result));
    }

    @GetMapping("/models/{id}")
    public ApiResponse<ModelDto> model(@PathVariable Long id) {
        return ApiResponse.ok(catalogService.getModel(id));
    }

    @GetMapping("/search")
    public ApiResponse<List<ModelDto>> search(@RequestParam(required = false) String q) {
        return ApiResponse.ok(catalogService.search(q));
    }

    @GetMapping("/aliases")
    public ApiResponse<List<String>> aliases(@RequestParam Long modelId) {
        return ApiResponse.ok(catalogService.aliases(modelId));
    }
}
