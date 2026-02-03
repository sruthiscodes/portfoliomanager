package com.portfolio.manager.dto;

import java.math.BigDecimal;
import java.util.Map;

import com.portfolio.manager.model.AssetType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSummaryDTO {

    private BigDecimal totalValue;

    private AssetDTO bestPerformer;

    private AssetDTO worstPerformer;

    private Map<AssetType, BigDecimal> allocationByType;

    private int assetCount;
}
