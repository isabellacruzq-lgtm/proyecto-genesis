package com.breaze.genesis.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registra cada ejecución de una operación con el costo descontado.
 * Permite al usuario ver su historial y al admin ver métricas globales.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operation_id", nullable = false)
    private CatalogOperation operation;

    @Column(name = "tokens_input", nullable = false)
    private Integer tokensInput;   // floor(len(input_json) / 4)

    @Column(name = "tokens_output", nullable = false)
    private Integer tokensOutput;  // floor(len(output_json) / 4)

    @Column(name = "base_cost", nullable = false)
    private Integer baseCost;

    @Column(name = "total_cost", nullable = false)
    private Integer totalCost;     // base + input + output

    @Column(name = "balance_before", nullable = false)
    private Integer balanceBefore;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public enum TransactionStatus {
        SUCCESS, FAILED_INSUFFICIENT_TOKENS, FAILED_OPERATION_ERROR
    }
}
