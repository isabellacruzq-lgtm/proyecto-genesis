package com.breaze.genesis.service.operation;

/**
 * Contrato que debe cumplir cualquier operación del catálogo.
 *
 * PRINCIPIO OPEN/CLOSED:
 * - Abierto para extensión: para añadir OP-05, solo hay que crear una nueva clase
 *   que implemente esta interfaz y registrarla como @Component.
 * - Cerrado para modificación: no se toca ninguna clase existente.
 *
 * El OperationRegistry descubre todas las implementaciones automáticamente
 * gracias al mecanismo de inyección de Spring, sin necesidad de modificar
 * ningún switch/if cuando se añada una nueva operación.
 */
public interface Operation {

    /**
     * Código único de la operación (ej: "OP-01").
     * Debe coincidir con el campo 'code' en la tabla catalog_operations.
     */
    String getCode();

    /**
     * Ejecuta la operación con el payload JSON de entrada
     * y retorna el resultado como JSON serializado.
     *
     * @param inputJson JSON de entrada como String (ya validado previamente)
     * @return JSON de salida como String
     * @throws OperationExecutionException si ocurre un error en el cálculo
     */
    String execute(String inputJson);
}
