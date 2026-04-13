package com.breaze.genesis.repository;

import com.breaze.genesis.entity.CatalogOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogOperationRepository extends JpaRepository<CatalogOperation, Long> {
    Optional<CatalogOperation> findByCode(String code);
    List<CatalogOperation> findByActiveTrue();
    Optional<CatalogOperation> findByCodeAndActiveTrue(String code);
}
