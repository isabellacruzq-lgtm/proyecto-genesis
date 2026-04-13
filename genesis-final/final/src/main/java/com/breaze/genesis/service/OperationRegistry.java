package com.breaze.genesis.service;

import com.breaze.genesis.service.operation.Operation;
import com.breaze.genesis.service.operation.OperationExecutionException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registro central de operaciones.
 *
 * PRINCIPIO OPEN/CLOSED en acción:
 * Spring inyecta automáticamente TODAS las clases que implementen la
 * interfaz Operation. Para añadir OP-05 basta con crear la clase
 * e implementar la interfaz — este registro la detecta sin modificarse.
 *
 * PRINCIPIO DE INVERSIÓN DE DEPENDENCIAS:
 * Este componente depende de la ABSTRACCIÓN (interfaz Operation),
 * no de ninguna implementación concreta.
 */
@Component
public class OperationRegistry {

    private final Map<String, Operation> operationsByCode;

    public OperationRegistry(List<Operation> operations) {
        this.operationsByCode = operations.stream()
                .collect(Collectors.toMap(Operation::getCode, Function.identity()));
    }

    /**
     * Retorna la operación correspondiente al código dado.
     * @throws OperationExecutionException si el código no existe en el registro.
     */
    public Operation getByCode(String code) {
        Operation op = operationsByCode.get(code);
        if (op == null) {
            throw new OperationExecutionException("Operación con código '" + code + "' no encontrada en el registro.");
        }
        return op;
    }

    public boolean supports(String code) {
        return operationsByCode.containsKey(code);
    }
}
