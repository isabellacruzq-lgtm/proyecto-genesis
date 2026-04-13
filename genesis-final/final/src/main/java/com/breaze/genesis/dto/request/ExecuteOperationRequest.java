package com.breaze.genesis.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Request para ejecutar cualquier operación del catálogo.
 * El campo 'params' es el payload libre que cada operación interpreta.
 */
@Getter
@Setter
public class ExecuteOperationRequest {

    @NotNull(message = "Los parámetros de la operación no pueden ser nulos")
    private Map<String, Object> params;
}
