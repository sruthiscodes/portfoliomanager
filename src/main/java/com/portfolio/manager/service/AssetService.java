package com.portfolio.manager.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.portfolio.manager.dto.AssetDTO;
import com.portfolio.manager.exception.ResourceNotFoundException;
import com.portfolio.manager.model.Asset;
import com.portfolio.manager.model.AssetType;
import com.portfolio.manager.repository.AssetRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final AssetRepository assetRepository;

    public List<AssetDTO> getAssets(Optional<AssetType> type) {
        List<Asset> assets = type.map(assetRepository::findByAssetType)
                .orElseGet(assetRepository::findAll);
        return assets.stream().map(this::toDto).collect(Collectors.toList());
    }

    public AssetDTO createAsset(AssetDTO request) {
        validateBusinessRules(request);
        Asset asset = Asset.builder()
                .symbol(request.getSymbol().trim())
                .name(request.getName().trim())
                .assetType(request.getAssetType())
                .quantity(request.getQuantity())
                .avgBuyPrice(request.getAvgBuyPrice())
                .currentPrice(request.getCurrentPrice())
                .build();
        Asset saved = assetRepository.save(asset);
        log.info("Asset created: {}", saved.getId());
        return toDto(saved);
    }

    public AssetDTO updateAsset(Long id, AssetDTO request) {
        validateBusinessRules(request);
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        asset.setSymbol(request.getSymbol().trim());
        asset.setName(request.getName().trim());
        asset.setAssetType(request.getAssetType());
        asset.setQuantity(request.getQuantity());
        asset.setAvgBuyPrice(request.getAvgBuyPrice());
        asset.setCurrentPrice(request.getCurrentPrice());
        Asset saved = assetRepository.save(asset);
        log.info("Asset updated: {}", saved.getId());
        return toDto(saved);
    }

    public void deleteAsset(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        assetRepository.delete(asset);
        log.info("Asset deleted: {}", id);
    }

    public AssetDTO getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        return toDto(asset);
    }

    private AssetDTO toDto(Asset asset) {
        BigDecimal currentPrice = Objects.requireNonNullElse(asset.getCurrentPrice(), ZERO);
        BigDecimal currentValue = asset.getQuantity().multiply(currentPrice);
        BigDecimal investedValue = asset.getQuantity().multiply(asset.getAvgBuyPrice());
        return AssetDTO.builder()
                .id(asset.getId())
                .symbol(asset.getSymbol())
                .name(asset.getName())
                .assetType(asset.getAssetType())
                .quantity(asset.getQuantity())
                .avgBuyPrice(asset.getAvgBuyPrice())
                .currentPrice(asset.getCurrentPrice())
                .currentValue(scaleMoney(currentValue))
                .investedValue(scaleMoney(investedValue))
                .build();
    }

    private void validateBusinessRules(AssetDTO request) {
        if (request.getCurrentPrice() != null && request.getCurrentPrice().compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Current price cannot be negative");
        }
        if (request.getQuantity() != null && request.getQuantity().compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (request.getAvgBuyPrice() != null && request.getAvgBuyPrice().compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("Average buy price must be greater than zero");
        }
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
