package com.breaze.genesis.service;

import com.breaze.genesis.dto.request.PlanRequest;
import com.breaze.genesis.dto.response.PlanResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlanService {
    PlanResponse create(PlanRequest request);
    Page<PlanResponse> findAllPaged(Pageable pageable);
    PlanResponse findById(Long id);
    PlanResponse update(Long id, PlanRequest request);
    void delete(Long id);
    void subscribe(Long planId, String userEmail);
}
