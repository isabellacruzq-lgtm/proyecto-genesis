package com.breaze.genesis.service;

import com.breaze.genesis.dto.response.MetricsResponse;
import com.breaze.genesis.dto.response.TransactionResponse;
import com.breaze.genesis.dto.response.UserProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    Page<TransactionResponse> getUserHistory(Long userId, Pageable pageable);
    MetricsResponse getGlobalMetrics(String from, String to, int topN);
    UserProfileResponse getUserProfile(String email);
}
