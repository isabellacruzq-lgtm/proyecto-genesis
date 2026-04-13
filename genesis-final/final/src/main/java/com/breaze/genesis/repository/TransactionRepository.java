package com.breaze.genesis.repository;

import com.breaze.genesis.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    // Tokens consumidos por día (para métricas admin)
    @Query("""
        SELECT DATE(t.createdAt) as day, SUM(t.totalCost) as totalTokens
        FROM Transaction t
        WHERE t.status = 'SUCCESS'
        AND t.createdAt BETWEEN :from AND :to
        GROUP BY DATE(t.createdAt)
        ORDER BY DATE(t.createdAt)
    """)
    List<Object[]> findTokensConsumedPerDay(@Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to);

    // Operaciones más ejecutadas
    @Query("""
        SELECT t.operation.code, t.operation.name, COUNT(t) as executions
        FROM Transaction t
        WHERE t.status = 'SUCCESS'
        GROUP BY t.operation.id, t.operation.code, t.operation.name
        ORDER BY executions DESC
    """)
    List<Object[]> findMostExecutedOperations(Pageable pageable);

    // Usuarios con mayor consumo
    @Query("""
        SELECT t.user.id, t.user.name, t.user.email, SUM(t.totalCost) as totalConsumed
        FROM Transaction t
        WHERE t.status = 'SUCCESS'
        GROUP BY t.user.id, t.user.name, t.user.email
        ORDER BY totalConsumed DESC
    """)
    List<Object[]> findTopConsumers(Pageable pageable);
}
