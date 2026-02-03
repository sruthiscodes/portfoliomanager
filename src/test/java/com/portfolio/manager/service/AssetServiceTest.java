package com.portfolio.manager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.portfolio.manager.dto.AssetDTO;
import com.portfolio.manager.exception.ResourceNotFoundException;
import com.portfolio.manager.model.Asset;
import com.portfolio.manager.model.AssetType;
import com.portfolio.manager.repository.AssetRepository;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;

    @Test
    void createAsset_success() {
        AssetDTO request = AssetDTO.builder()
                .symbol("AAPL")
                .name("Apple")
                .assetType(AssetType.STOCK)
                .quantity(new BigDecimal("10.0000"))
                .avgBuyPrice(new BigDecimal("150.00"))
                .currentPrice(new BigDecimal("170.00"))
                .build();

        Asset saved = Asset.builder()
                .id(1L)
                .symbol("AAPL")
                .name("Apple")
                .assetType(AssetType.STOCK)
                .quantity(new BigDecimal("10.0000"))
                .avgBuyPrice(new BigDecimal("150.00"))
                .currentPrice(new BigDecimal("170.00"))
                .build();

        when(assetRepository.save(any(Asset.class))).thenReturn(saved);

        AssetDTO result = assetService.createAsset(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCurrentValue()).isEqualTo(new BigDecimal("1700.00"));
        assertThat(result.getInvestedValue()).isEqualTo(new BigDecimal("1500.00"));
        verify(assetRepository).save(any(Asset.class));
    }

    @Test
    void updateAsset_notFound() {
        AssetDTO request = AssetDTO.builder()
                .symbol("AAPL")
                .name("Apple")
                .assetType(AssetType.STOCK)
                .quantity(new BigDecimal("10.0000"))
                .avgBuyPrice(new BigDecimal("150.00"))
                .currentPrice(new BigDecimal("170.00"))
                .build();

        when(assetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assetService.updateAsset(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Asset not found");
    }

    @Test
    void getAssets_byType() {
        Asset asset = Asset.builder()
                .id(1L)
                .symbol("BND")
                .name("Bond Fund")
                .assetType(AssetType.BOND)
                .quantity(new BigDecimal("5.0000"))
                .avgBuyPrice(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal("110.00"))
                .build();

        when(assetRepository.findByAssetType(AssetType.BOND)).thenReturn(List.of(asset));

        List<AssetDTO> assets = assetService.getAssets(Optional.of(AssetType.BOND));

        assertThat(assets).hasSize(1);
        assertThat(assets.get(0).getSymbol()).isEqualTo("BND");
    }

    @Test
    void deleteAsset_success() {
        Asset asset = Asset.builder().id(1L).build();
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));

        assetService.deleteAsset(1L);

        verify(assetRepository).delete(asset);
    }

    @Test
    void deleteAsset_notFound() {
        when(assetRepository.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assetService.deleteAsset(55L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Asset not found");
    }
}
