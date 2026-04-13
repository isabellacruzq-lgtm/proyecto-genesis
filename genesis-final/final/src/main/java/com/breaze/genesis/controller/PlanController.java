package com.breaze.genesis.controller;

import com.breaze.genesis.dto.request.PlanRequest;
import com.breaze.genesis.dto.response.ApiResponse;
import com.breaze.genesis.dto.response.PlanResponse;
import com.breaze.genesis.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
@Tag(name = "Planes", description = "Gestión de planes de suscripción")
@SecurityRequirement(name = "bearerAuth")
public class PlanController {

    private final PlanService planService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Listar planes disponibles")
    public ResponseEntity<ApiResponse<Page<PlanResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(planService.findAllPaged(pageable), "Planes disponibles"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Consultar un plan por ID")
    public ResponseEntity<ApiResponse<PlanResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(planService.findById(id), "Plan encontrado"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear nuevo plan (admin)")
    public ResponseEntity<ApiResponse<PlanResponse>> create(@Valid @RequestBody PlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(planService.create(request), "Plan creado"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Editar un plan (admin)")
    public ResponseEntity<ApiResponse<PlanResponse>> update(@PathVariable Long id,
                                                             @Valid @RequestBody PlanRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(planService.update(id, request), "Plan actualizado"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar un plan (admin) - falla si tiene suscripciones activas")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        planService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Plan eliminado"));
    }

    @PostMapping("/{id}/subscribe")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Suscribirse a un plan")
    public ResponseEntity<ApiResponse<Void>> subscribe(@PathVariable Long id,
                                                        @org.springframework.security.core.annotation.AuthenticationPrincipal
                                                        org.springframework.security.core.userdetails.UserDetails userDetails) {
        planService.subscribe(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null, "Suscripción realizada"));
    }
}
