package com.portfolio.manager.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.portfolio.manager.dto.AssetDTO;
import com.portfolio.manager.dto.PortfolioSummaryDTO;
import com.portfolio.manager.model.Asset;
import com.portfolio.manager.model.AssetType;
import com.portfolio.manager.repository.AssetRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final AssetRepository assetRepository;

    public PortfolioSummaryDTO getPortfolioSummary() {
        List<Asset> assets = assetRepository.findAll();
        if (assets.isEmpty()) {
            return PortfolioSummaryDTO.builder()
                    .totalValue(ZERO)
                    .allocationByType(Map.of())
                    .assetCount(0)
                    .build();
        }

        BigDecimal totalValue = assets.stream()
                .map(this::currentValue)
                .reduce(ZERO, BigDecimal::add);

        Asset best = assets.stream()
                .max(Comparator.comparing(this::gainValue))
                .orElse(null);
        Asset worst = assets.stream()
                .min(Comparator.comparing(this::gainValue))
                .orElse(null);

        Map<AssetType, BigDecimal> allocation = calculateAllocation(assets, totalValue);

        log.info("Calculated portfolio summary for {} assets", assets.size());

        return PortfolioSummaryDTO.builder()
                .totalValue(scaleMoney(totalValue))
                .bestPerformer(toDto(best))
                .worstPerformer(toDto(worst))
                .allocationByType(allocation)
                .assetCount(assets.size())
                .build();
    }

    private Map<AssetType, BigDecimal> calculateAllocation(List<Asset> assets, BigDecimal totalValue) {
        Map<AssetType, BigDecimal> allocation = new EnumMap<>(AssetType.class);
        for (Asset asset : assets) {
            BigDecimal currentValue = currentValue(asset);
            allocation.merge(asset.getAssetType(), currentValue, BigDecimal::add);
        }
        for (Map.Entry<AssetType, BigDecimal> entry : allocation.entrySet()) {
            BigDecimal percentage = totalValue.compareTo(ZERO) == 0
                    ? ZERO
                    : entry.getValue().multiply(ONE_HUNDRED).divide(totalValue, 2, RoundingMode.HALF_UP);
            entry.setValue(percentage);
        }
        return allocation;
    }

    private BigDecimal currentValue(Asset asset) {
        BigDecimal currentPrice = Objects.requireNonNullElse(asset.getCurrentPrice(), ZERO);
        return asset.getQuantity().multiply(currentPrice);
    }

    private BigDecimal gainValue(Asset asset) {
        BigDecimal currentValue = currentValue(asset);
        BigDecimal investedValue = asset.getQuantity().multiply(asset.getAvgBuyPrice());
        return currentValue.subtract(investedValue);
    }

    private AssetDTO toDto(Asset asset) {
        if (asset == null) {
            return null;
        }
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

    private BigDecimal scaleMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
