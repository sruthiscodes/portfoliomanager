package com.portfolio.manager.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PriceUpdateService {

    public String refreshPrices() {
        log.info("Price refresh triggered (placeholder integration)");
        return "Price refresh triggered";
    }
}
