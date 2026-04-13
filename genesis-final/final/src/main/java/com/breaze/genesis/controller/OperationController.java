package com.breaze.genesis.controller;

import com.breaze.genesis.dto.request.ExecuteOperationRequest;
import com.breaze.genesis.dto.response.ApiResponse;
import com.breaze.genesis.dto.response.CatalogOperationResponse;
import com.breaze.genesis.dto.response.ExecuteOperationResponse;
import com.breaze.genesis.repository.UserRepository;
import com.breaze.genesis.service.OperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/operations")
@RequiredArgsConstructor
@Tag(name = "Operaciones", description = "Catálogo y ejecución de operaciones")
@SecurityRequirement(name = "bearerAuth")
public class OperationController {

    private final OperationService operationService;
    private final UserRepository   userRepository;

    @GetMapping("/catalog")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Consultar catálogo de operaciones activas")
    public ResponseEntity<ApiResponse<List<CatalogOperationResponse>>> getCatalog() {
        return ResponseEntity.ok(ApiResponse.ok(operationService.getActiveCatalog(), "Catálogo de operaciones"));
    }

    @GetMapping("/catalog/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Catálogo completo con paginación (admin)")
    public ResponseEntity<ApiResponse<Page<CatalogOperationResponse>>> getFullCatalog(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(operationService.getFullCatalog(pageable), "Catálogo completo"));
    }

    @PostMapping("/{code}/execute")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Ejecutar una operación del catálogo")
    public ResponseEntity<ApiResponse<ExecuteOperationResponse>> execute(
            @PathVariable String code,
            @Valid @RequestBody ExecuteOperationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
                .getId();

        return ResponseEntity.ok(ApiResponse.ok(
                operationService.execute(code, request, userId),
                "Operación ejecutada exitosamente"));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar o desactivar una operación (admin)")
    public ResponseEntity<ApiResponse<CatalogOperationResponse>> toggle(
            @PathVariable Long id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(ApiResponse.ok(
                operationService.toggleOperation(id, active),
                active ? "Operación activada" : "Operación desactivada"));
    }
}
