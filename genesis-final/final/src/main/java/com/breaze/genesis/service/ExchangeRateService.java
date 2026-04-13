package com.breaze.genesis.service;

import com.breaze.genesis.dto.response.ExchangeRateResponse;
import java.math.BigDecimal;

public interface ExchangeRateService {
    ExchangeRateResponse updateRate(BigDecimal value, String adminEmail);
    ExchangeRateResponse getCurrentRateResponse();
}
