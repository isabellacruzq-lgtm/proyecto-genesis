package com.breaze.genesis.controller;

import com.breaze.genesis.dto.response.ApiResponse;
import com.breaze.genesis.dto.response.UserResponse;
import com.breaze.genesis.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administración", description = "Gestión de usuarios y tokens (solo admin)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "Listar todos los usuarios con saldo, plan y estado")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> listUsers(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listUsers(pageable), "Usuarios"));
    }

    @PatchMapping("/users/{id}/activate")
    @Operation(summary = "Activar un usuario")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        adminService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Usuario activado"));
    }

    @PatchMapping("/users/{id}/deactivate")
    @Operation(summary = "Desactivar un usuario")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        adminService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Usuario desactivado"));
    }

    @PostMapping("/users/{id}/recharge")
    @Operation(summary = "Recargar tokens a un usuario manualmente")
    public ResponseEntity<ApiResponse<Void>> recharge(@PathVariable Long id,
                                                       @RequestParam int amount) {
        adminService.rechargeTokens(id, amount);
        return ResponseEntity.ok(ApiResponse.ok(null, amount + " tokens recargados"));
    }

    @PostMapping("/users/{id}/plan/{planId}")
    @Operation(summary = "Asignar plan a un usuario")
    public ResponseEntity<ApiResponse<Void>> assignPlan(@PathVariable Long id,
                                                         @PathVariable Long planId) {
        adminService.assignPlan(id, planId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Plan asignado"));
    }
}
