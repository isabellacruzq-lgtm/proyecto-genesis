package com.breaze.genesis.service.impl;

import com.breaze.genesis.dto.response.ExchangeRateResponse;
import com.breaze.genesis.entity.ExchangeRate;
import com.breaze.genesis.entity.User;
import com.breaze.genesis.repository.ExchangeRateRepository;
import com.breaze.genesis.repository.UserRepository;
import com.breaze.genesis.service.ExchangeRateService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final UserRepository         userRepository;

    @Override
    @Transactional
    public ExchangeRateResponse updateRate(BigDecimal value, String adminEmail) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("La tasa debe ser mayor a 0");

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new EntityNotFoundException("Admin no encontrado"));

        ExchangeRate rate = exchangeRateRepository.findTopByOrderByUpdatedAtDesc()
                .orElse(ExchangeRate.builder().build());

        rate.setCopPerUsd(value);
        rate.setUpdatedBy(admin);
        return toResponse(exchangeRateRepository.save(rate));
    }

    @Override
    @Transactional(readOnly = true)
    public ExchangeRateResponse getCurrentRateResponse() {
        ExchangeRate rate = exchangeRateRepository.findTopByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new EntityNotFoundException("No hay tasa de cambio registrada"));
        return toResponse(rate);
    }

    private ExchangeRateResponse toResponse(ExchangeRate r) {
        return ExchangeRateResponse.builder()
                .id(r.getId())
                .copPerUsd(r.getCopPerUsd())
                .updatedAt(r.getUpdatedAt())
                .updatedBy(r.getUpdatedBy() != null ? r.getUpdatedBy().getEmail() : null)
                .build();
    }
}
