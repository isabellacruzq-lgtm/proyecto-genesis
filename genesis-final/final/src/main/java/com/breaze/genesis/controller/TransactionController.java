package com.breaze.genesis.controller;

import com.breaze.genesis.dto.response.ApiResponse;
import com.breaze.genesis.dto.response.MetricsResponse;
import com.breaze.genesis.dto.response.TransactionResponse;
import com.breaze.genesis.dto.response.UserProfileResponse;
import com.breaze.genesis.repository.UserRepository;
import com.breaze.genesis.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Transacciones y Perfil", description = "Historial, perfil y métricas")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository     userRepository;

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Consultar perfil propio con saldo y plan activo")
    public ResponseEntity<ApiResponse<UserProfileResponse>> profile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                transactionService.getUserProfile(userDetails.getUsername()), "Perfil"));
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Historial de transacciones propias (paginado)")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> history(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Long userId = userRepository.findByEmail(userDetails.getUsername()).orElseThrow().getId();
        return ResponseEntity.ok(ApiResponse.ok(
                transactionService.getUserHistory(userId, pageable), "Historial"));
    }

    @GetMapping("/admin/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Métricas globales de consumo (admin)")
    public ResponseEntity<ApiResponse<MetricsResponse>> metrics(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "5") int topN) {
        return ResponseEntity.ok(ApiResponse.ok(
                transactionService.getGlobalMetrics(from, to, topN), "Métricas globales"));
    }
}
