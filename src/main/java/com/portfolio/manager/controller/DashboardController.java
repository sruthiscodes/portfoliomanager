package com.portfolio.manager.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.manager.dto.PortfolioSummaryDTO;
import com.portfolio.manager.service.DashboardService;
import com.portfolio.manager.service.PriceUpdateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;
    private final PriceUpdateService priceUpdateService;

    @GetMapping("/dashboard")
    public ResponseEntity<PortfolioSummaryDTO> getDashboard() {
        log.info("GET /api/dashboard");
        return ResponseEntity.ok(dashboardService.getPortfolioSummary());
    }

    @GetMapping("/prices/update")
    public ResponseEntity<Map<String, String>> updatePrices() {
        log.info("GET /api/prices/update");
        String message = priceUpdateService.refreshPrices();
        return ResponseEntity.ok(Map.of("message", message));
    }
}
