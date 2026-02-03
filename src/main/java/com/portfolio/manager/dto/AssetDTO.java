package com.portfolio.manager.dto;

import java.math.BigDecimal;

import com.portfolio.manager.model.AssetType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetDTO {

    private Long id;

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Asset type is required")
    private AssetType assetType;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than zero")
    @Digits(integer = 11, fraction = 4, message = "Quantity must have up to 11 digits and 4 decimals")
    private BigDecimal quantity;

    @NotNull(message = "Average buy price is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Average buy price must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Average buy price must have up to 13 digits and 2 decimals")
    private BigDecimal avgBuyPrice;

    @DecimalMin(value = "0.00", message = "Current price must be zero or greater")
    @Digits(integer = 13, fraction = 2, message = "Current price must have up to 13 digits and 2 decimals")
    private BigDecimal currentPrice;

    private BigDecimal currentValue;

    private BigDecimal investedValue;
}
