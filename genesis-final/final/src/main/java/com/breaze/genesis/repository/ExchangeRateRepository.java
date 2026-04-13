package com.breaze.genesis.repository;

import com.breaze.genesis.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    // El registro activo siempre tendrá id = 1
    Optional<ExchangeRate> findTopByOrderByUpdatedAtDesc();
}
