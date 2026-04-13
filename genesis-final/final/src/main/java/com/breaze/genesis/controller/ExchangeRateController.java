package com.breaze.genesis.controller;

import com.breaze.genesis.dto.response.ApiResponse;
import com.breaze.genesis.dto.response.ExchangeRateResponse;
import com.breaze.genesis.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/admin/exchange-rate")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Tasa de Cambio", description = "Gestión de tasa COP/USD (solo admin)")
@SecurityRequirement(name = "bearerAuth")
public class ExchangeRateController {

    private final ExchangeRateService service;

    @GetMapping
    @Operation(summary = "Consultar tasa de cambio vigente")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> get() {
        return ResponseEntity.ok(ApiResponse.ok(service.getCurrentRateResponse(), "Tasa vigente"));
    }

    @PutMapping
    @Operation(summary = "Actualizar tasa COP/USD")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> update(
            @RequestParam BigDecimal value,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                service.updateRate(value, userDetails.getUsername()),
                "Tasa actualizada"));
    }
}
