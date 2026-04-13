package com.breaze.genesis.service.impl;

import com.breaze.genesis.dto.request.ExecuteOperationRequest;
import com.breaze.genesis.dto.response.CatalogOperationResponse;
import com.breaze.genesis.dto.response.ExecuteOperationResponse;
import com.breaze.genesis.entity.CatalogOperation;
import com.breaze.genesis.entity.Transaction;
import com.breaze.genesis.entity.User;
import com.breaze.genesis.repository.CatalogOperationRepository;
import com.breaze.genesis.repository.TransactionRepository;
import com.breaze.genesis.repository.UserRepository;
import com.breaze.genesis.service.OperationRegistry;
import com.breaze.genesis.service.OperationService;
import com.breaze.genesis.service.TokenCalculator;
import com.breaze.genesis.service.operation.Operation;
import com.breaze.genesis.service.operation.OperationExecutionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de operaciones.
 *
 * PRINCIPIO SRP: orquesta la ejecución pero delega:
 *   - el cálculo matemático    → cada clase Operation
 *   - el cálculo de tokens     → TokenCalculator
 *   - la búsqueda de operación → OperationRegistry
 *
 * PRINCIPIO DIP: depende de interfaces (OperationService, Operation),
 *   no de implementaciones concretas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationServiceImpl implements OperationService {

    private final OperationRegistry          operationRegistry;
    private final TokenCalculator            tokenCalculator;
    private final CatalogOperationRepository catalogOperationRepository;
    private final TransactionRepository      transactionRepository;
    private final UserRepository             userRepository;
    private final ObjectMapper               objectMapper;

    @Override
    @Transactional
    public ExecuteOperationResponse execute(String operationCode,
                                            ExecuteOperationRequest request,
                                            Long userId) {

        // 1. Verificar que la operación existe en BD y está activa
        CatalogOperation catalogOp = catalogOperationRepository
                .findByCodeAndActiveTrue(operationCode)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Operación '" + operationCode + "' no encontrada o no está activa"));

        // 2. Cargar usuario y verificar saldo (pre-check: solo costo base)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        String inputJson;
        try {
            inputJson = objectMapper.writeValueAsString(request.getParams());
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar los parámetros de entrada");
        }

        int tokensInput  = tokenCalculator.calculateInputTokens(inputJson);
        int estimatedMin = catalogOp.getBaseCost() + tokensInput;

        if (user.getTokenBalance() < estimatedMin) {
            // Registrar transacción fallida por saldo insuficiente (sin descontar)
            saveFailedTransaction(user, catalogOp, tokensInput, 0,
                    Transaction.TransactionStatus.FAILED_INSUFFICIENT_TOKENS,
                    "Saldo insuficiente. Se necesitan al menos " + estimatedMin + " tokens.");

            throw new IllegalStateException(
                    "Saldo insuficiente. Tienes " + user.getTokenBalance() +
                    " tokens y la operación requiere al menos " + estimatedMin + " tokens.");
        }

        // 3. Ejecutar la operación
        Operation operation = operationRegistry.getByCode(operationCode);
        String outputJson;

        try {
            outputJson = operation.execute(inputJson);
        } catch (OperationExecutionException e) {
            // Error en el cálculo: NO se descuentan tokens
            log.error("Error ejecutando operación {}: {}", operationCode, e.getMessage());
            saveFailedTransaction(user, catalogOp, tokensInput, 0,
                    Transaction.TransactionStatus.FAILED_OPERATION_ERROR, e.getMessage());
            throw e;
        }

        // 4. Calcular costo total real (con tokens de salida)
        int tokensOutput = tokenCalculator.calculateOutputTokens(outputJson);
        int totalCost    = tokenCalculator.calculateTotal(catalogOp.getBaseCost(), tokensInput, tokensOutput);

        // 5. Verificar saldo con costo real
        if (user.getTokenBalance() < totalCost) {
            saveFailedTransaction(user, catalogOp, tokensInput, tokensOutput,
                    Transaction.TransactionStatus.FAILED_INSUFFICIENT_TOKENS,
                    "Saldo insuficiente para cubrir el costo total de " + totalCost + " tokens.");
            throw new IllegalStateException(
                    "Saldo insuficiente para cubrir el costo total de " + totalCost + " tokens.");
        }

        // 6. Descontar tokens y guardar transacción exitosa
        int balanceBefore = user.getTokenBalance();
        int balanceAfter  = balanceBefore - totalCost;
        user.setTokenBalance(balanceAfter);
        userRepository.save(user);

        Transaction tx = Transaction.builder()
                .user(user)
                .operation(catalogOp)
                .tokensInput(tokensInput)
                .tokensOutput(tokensOutput)
                .baseCost(catalogOp.getBaseCost())
                .totalCost(totalCost)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .status(Transaction.TransactionStatus.SUCCESS)
                .build();
        transactionRepository.save(tx);

        // 7. Construir respuesta
        Map<String, Object> resultMap;
        try {
            resultMap = objectMapper.readValue(outputJson, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Error al deserializar el resultado de la operación");
        }

        return ExecuteOperationResponse.builder()
                .operationCode(catalogOp.getCode())
                .operationName(catalogOp.getName())
                .result(resultMap)
                .tokensInput(tokensInput)
                .tokensOutput(tokensOutput)
                .baseCost(catalogOp.getBaseCost())
                .totalCost(totalCost)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .executedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogOperationResponse> getActiveCatalog() {
        return catalogOperationRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CatalogOperationResponse> getFullCatalog(Pageable pageable) {
        return catalogOperationRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public CatalogOperationResponse toggleOperation(Long operationId, boolean active) {
        CatalogOperation op = catalogOperationRepository.findById(operationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Operación con id " + operationId + " no encontrada"));
        op.setActive(active);
        return toResponse(catalogOperationRepository.save(op));
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private void saveFailedTransaction(User user, CatalogOperation op,
                                       int tokensInput, int tokensOutput,
                                       Transaction.TransactionStatus status,
                                       String errorMessage) {
        Transaction tx = Transaction.builder()
                .user(user)
                .operation(op)
                .tokensInput(tokensInput)
                .tokensOutput(tokensOutput)
                .baseCost(op.getBaseCost())
                .totalCost(0)
                .balanceBefore(user.getTokenBalance())
                .balanceAfter(user.getTokenBalance())
                .status(status)
                .errorMessage(errorMessage)
                .build();
        transactionRepository.save(tx);
    }

    private CatalogOperationResponse toResponse(CatalogOperation op) {
        return CatalogOperationResponse.builder()
                .id(op.getId())
                .code(op.getCode())
                .name(op.getName())
                .description(op.getDescription())
                .baseCost(op.getBaseCost())
                .active(op.getActive())
                .createdAt(op.getCreatedAt())
                .updatedAt(op.getUpdatedAt())
                .build();
    }
}
