package com.breaze.genesis.service.operation;

/**
 * Se lanza cuando ocurre un error durante el cálculo de una operación.
 * Según las reglas del negocio, si ocurre esta excepción NO se descuentan tokens.
 */
public class OperationExecutionException extends RuntimeException {
    public OperationExecutionException(String message) {
        super(message);
    }
    public OperationExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
