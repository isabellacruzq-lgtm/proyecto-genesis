package com.breaze.genesis.service;

import com.breaze.genesis.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {
    Page<UserResponse> listUsers(Pageable pageable);
    void activateUser(Long userId);
    void deactivateUser(Long userId);
    void rechargeTokens(Long userId, int amount);
    void assignPlan(Long userId, Long planId);
}
