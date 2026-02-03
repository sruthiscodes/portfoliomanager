package com.portfolio.manager.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.portfolio.manager.dto.AssetDTO;
import com.portfolio.manager.dto.ImportResultDTO;
import com.portfolio.manager.model.AssetType;
import com.portfolio.manager.service.AssetService;
import com.portfolio.manager.service.FileImportService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AssetController {

    private final AssetService assetService;
    private final FileImportService fileImportService;

    @GetMapping
    public ResponseEntity<List<AssetDTO>> getAssets(@RequestParam(name = "type", required = false) AssetType type) {
        log.info("GET /api/assets type={}", type);
        List<AssetDTO> assets = assetService.getAssets(Optional.ofNullable(type));
        return ResponseEntity.ok(assets);
    }

    @PostMapping
    public ResponseEntity<AssetDTO> createAsset(@Valid @RequestBody AssetDTO request) {
        log.info("POST /api/assets symbol={}", request.getSymbol());
        AssetDTO created = assetService.createAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetDTO> updateAsset(@PathVariable Long id, @Valid @RequestBody AssetDTO request) {
        log.info("PUT /api/assets/{}", id);
        AssetDTO updated = assetService.updateAsset(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        log.info("DELETE /api/assets/{}", id);
        assetService.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<ImportResultDTO> importAssets(@RequestParam("file") MultipartFile file) {
        log.info("POST /api/assets/import filename={}", file.getOriginalFilename());
        try {
            ImportResultDTO result = fileImportService.importFile(file);
            log.info("Import completed: {} success, {} failed out of {} total rows", 
                     result.getSuccessCount(), result.getFailureCount(), result.getTotalRows());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Import failed: {}", e.getMessage());
            throw new IllegalArgumentException("Import failed: " + e.getMessage());
        }
    }
}
