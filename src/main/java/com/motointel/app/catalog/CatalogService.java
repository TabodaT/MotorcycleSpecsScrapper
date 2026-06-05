package com.motointel.app.catalog;

import com.motointel.app.common.ApiException;
import com.motointel.app.domain.Manufacturer;
import com.motointel.app.domain.ModelAlias;
import com.motointel.app.domain.MotorcycleImage;
import com.motointel.app.domain.MotorcycleModel;
import com.motointel.app.domain.MotorcycleSpec;
import com.motointel.app.domain.MotorcycleVariant;
import com.motointel.app.dto.ImageDto;
import com.motointel.app.dto.ManufacturerDto;
import com.motointel.app.dto.ModelDto;
import com.motointel.app.repo.ManufacturerRepository;
import com.motointel.app.repo.ModelAliasRepository;
import com.motointel.app.repo.MotorcycleImageRepository;
import com.motointel.app.repo.MotorcycleModelRepository;
import com.motointel.app.repo.MotorcycleSpecRepository;
import com.motointel.app.repo.MotorcycleVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final ManufacturerRepository manufacturerRepo;
    private final MotorcycleModelRepository modelRepo;
    private final MotorcycleVariantRepository variantRepo;
    private final MotorcycleSpecRepository specRepo;
    private final MotorcycleImageRepository imageRepo;
    private final ModelAliasRepository aliasRepo;
    private final JdbcTemplate jdbc;

    public CatalogService(ManufacturerRepository manufacturerRepo, MotorcycleModelRepository modelRepo,
                          MotorcycleVariantRepository variantRepo, MotorcycleSpecRepository specRepo,
                          MotorcycleImageRepository imageRepo, ModelAliasRepository aliasRepo, JdbcTemplate jdbc) {
        this.manufacturerRepo = manufacturerRepo;
        this.modelRepo = modelRepo;
        this.variantRepo = variantRepo;
        this.specRepo = specRepo;
        this.imageRepo = imageRepo;
        this.aliasRepo = aliasRepo;
        this.jdbc = jdbc;
    }

    public List<ManufacturerDto> listManufacturers() {
        return manufacturerRepo.findAll().stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .map(m -> new ManufacturerDto(m.getId(), m.getName(), m.getNormalizedName(),
                        modelRepo.countByManufacturerId(m.getId())))
                .toList();
    }

    public Page<ModelDto> listModels(Long manufacturerId, String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MotorcycleModel> models = modelRepo.search(manufacturerId, blankToNull(q), pageable);
        Map<Long, String> manufacturerNames = manufacturerNameMap();
        return models.map(m -> lightModel(m, manufacturerNames.get(m.getManufacturerId())));
    }

    public List<ModelDto> search(String q) {
        if (blankToNull(q) == null) {
            return List.of();
        }
        Map<Long, String> manufacturerNames = manufacturerNameMap();
        return modelRepo.quickSearch(q, PageRequest.of(0, 20)).stream()
                .map(m -> lightModel(m, manufacturerNames.get(m.getManufacturerId())))
                .toList();
    }

    public List<String> aliases(Long modelId) {
        return aliasRepo.findByModelId(modelId).stream().map(ModelAlias::getAlias).toList();
    }

    public ModelDto getModel(Long id) {
        MotorcycleModel m = modelRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("Model not found: " + id));
        String manufacturerName = manufacturerRepo.findById(m.getManufacturerId())
                .map(Manufacturer::getName).orElse(null);

        List<ModelDto.Variant> variants = variantRepo.findByModelId(id).stream()
                .map(this::toVariant).toList();
        List<ModelDto.Spec> specs = specRepo.findByModelId(id).stream()
                .map(this::toSpec).toList();
        List<ImageDto> images = imageRepo.findByModelId(id).stream()
                .map(this::toImage).toList();
        List<String> aliases = aliases(id);
        List<ModelDto.SourceRef> sources = sourceRefs(id);

        return new ModelDto(m.getId(), manufacturerName, m.getName(), m.getNormalizedName(),
                m.getProductionStartYear(), m.getProductionEndYear(),
                variants, specs, images, aliases, sources);
    }

    private ModelDto lightModel(MotorcycleModel m, String manufacturerName) {
        return new ModelDto(m.getId(), manufacturerName, m.getName(), m.getNormalizedName(),
                m.getProductionStartYear(), m.getProductionEndYear(),
                List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private ModelDto.Variant toVariant(MotorcycleVariant v) {
        return new ModelDto.Variant(v.getId(), v.getName(), v.getYearFrom(), v.getYearTo());
    }

    private ModelDto.Spec toSpec(MotorcycleSpec s) {
        return new ModelDto.Spec(s.getEngineCapacityCc(), s.getPowerKw(), s.getTorqueNm(),
                s.getDryWeightKg(), s.getWetWeightKg(), s.getSeatHeightMm(), s.getFuelCapacityL(),
                s.getTopSpeedKmh(), s.getCooling(), s.getTransmission(), s.getFinalDrive(),
                s.getAbs(), s.isElectric());
    }

    private ImageDto toImage(MotorcycleImage img) {
        return new ImageDto(img.getId(), img.getStorageRef(), img.getSourceUrl(),
                com.motointel.app.storage.MediaUrls.forRef(img.getStorageRef()));
    }

    private List<ModelDto.SourceRef> sourceRefs(Long modelId) {
        return jdbc.query("""
                SELECT DISTINCT p.url AS url, s.fetched_at AS fetched_at
                FROM spec_evidence e
                JOIN motorcycle_specs sp ON sp.id = e.spec_id
                JOIN catalog_source_snapshots s ON s.id = e.snapshot_id
                JOIN catalog_source_pages p ON p.id = s.page_id
                WHERE sp.model_id = ?
                """,
                (rs, n) -> new ModelDto.SourceRef(rs.getString("url"),
                        rs.getTimestamp("fetched_at") == null ? null : rs.getTimestamp("fetched_at").toInstant()),
                modelId);
    }

    private Map<Long, String> manufacturerNameMap() {
        return manufacturerRepo.findAll().stream()
                .collect(Collectors.toMap(Manufacturer::getId, Manufacturer::getName));
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
