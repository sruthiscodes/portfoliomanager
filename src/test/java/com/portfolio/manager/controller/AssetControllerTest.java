package com.portfolio.manager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.manager.dto.AssetDTO;
import com.portfolio.manager.exception.GlobalExceptionHandler;
import com.portfolio.manager.exception.ResourceNotFoundException;
import com.portfolio.manager.model.AssetType;
import com.portfolio.manager.service.AssetService;

@WebMvcTest(controllers = AssetController.class)
@Import(GlobalExceptionHandler.class)
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssetService assetService;

    @Test
    void getAssets_success() throws Exception {
        AssetDTO asset = AssetDTO.builder()
                .id(1L)
                .symbol("AAPL")
                .name("Apple")
                .assetType(AssetType.STOCK)
                .quantity(new BigDecimal("10.0000"))
                .avgBuyPrice(new BigDecimal("150.00"))
                .currentPrice(new BigDecimal("170.00"))
                .currentValue(new BigDecimal("1700.00"))
                .investedValue(new BigDecimal("1500.00"))
                .build();

        when(assetService.getAssets(any())).thenReturn(List.of(asset));

        mockMvc.perform(get("/api/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].symbol").value("AAPL"));
    }

    @Test
    void createAsset_success() throws Exception {
        AssetDTO request = AssetDTO.builder()
                .symbol("AAPL")
                .name("Apple")
                .assetType(AssetType.STOCK)
                .quantity(new BigDecimal("10.0000"))
                .avgBuyPrice(new BigDecimal("150.00"))
                .currentPrice(new BigDecimal("170.00"))
                .build();

        AssetDTO response = AssetDTO.builder()
                .id(1L)
                .symbol("AAPL")
                .name("Apple")
                .assetType(AssetType.STOCK)
                .quantity(new BigDecimal("10.0000"))
                .avgBuyPrice(new BigDecimal("150.00"))
                .currentPrice(new BigDecimal("170.00"))
                .currentValue(new BigDecimal("1700.00"))
                .investedValue(new BigDecimal("1500.00"))
                .build();

        when(assetService.createAsset(any(AssetDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.symbol").value("AAPL"));
    }

    @Test
    void updateAsset_notFound() throws Exception {
        AssetDTO request = AssetDTO.builder()
                .symbol("AAPL")
                .name("Apple")
                .assetType(AssetType.STOCK)
                .quantity(new BigDecimal("10.0000"))
                .avgBuyPrice(new BigDecimal("150.00"))
                .currentPrice(new BigDecimal("170.00"))
                .build();

        when(assetService.updateAsset(eq(99L), any(AssetDTO.class)))
                .thenThrow(new ResourceNotFoundException("Asset not found"));

        mockMvc.perform(put("/api/assets/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Asset not found"))
                .andExpect(jsonPath("$.path").value("/api/assets/99"));
    }

    @Test
    void createAsset_validationFailure() throws Exception {
        AssetDTO request = AssetDTO.builder()
                .symbol(" ")
                .name("Apple")
                .assetType(AssetType.STOCK)
                .quantity(new BigDecimal("10.0000"))
                .avgBuyPrice(new BigDecimal("150.00"))
                .currentPrice(new BigDecimal("170.00"))
                .build();

        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
