package com.breaze.genesis.service;

import com.breaze.genesis.dto.request.ExecuteOperationRequest;
import com.breaze.genesis.dto.response.ExecuteOperationResponse;
import com.breaze.genesis.dto.response.CatalogOperationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Contrato del servicio de operaciones.
 *
 * PRINCIPIO ISP: interfaz específica para el dominio de operaciones.
 * PRINCIPIO DIP: el controlador depende de esta abstracción, no de la implementación.
 */
public interface OperationService {

    /**
     * Ejecuta una operación del catálogo descontando tokens al usuario autenticado.
     */
    ExecuteOperationResponse execute(String operationCode, ExecuteOperationRequest request, Long userId);

    /**
     * Retorna el catálogo de operaciones activas (para usuario final).
     */
    List<CatalogOperationResponse> getActiveCatalog();

    /**
     * Retorna todo el catálogo (para administrador).
     */
    Page<CatalogOperationResponse> getFullCatalog(Pageable pageable);

    /**
     * Activa o desactiva una operación (solo admin).
     */
    CatalogOperationResponse toggleOperation(Long operationId, boolean active);
}
