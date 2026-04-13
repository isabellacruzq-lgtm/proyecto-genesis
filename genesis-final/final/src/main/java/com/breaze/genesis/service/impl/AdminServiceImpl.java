package com.breaze.genesis.service.impl;

import com.breaze.genesis.dto.response.UserResponse;
import com.breaze.genesis.entity.Plan;
import com.breaze.genesis.entity.User;
import com.breaze.genesis.repository.PlanRepository;
import com.breaze.genesis.repository.UserRepository;
import com.breaze.genesis.service.AdminService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public void activateUser(Long userId) {
        User user = getUser(userId);
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivateUser(Long userId) {
        User user = getUser(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void rechargeTokens(Long userId, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("El monto de recarga debe ser mayor a 0");
        User user = getUser(userId);
        user.setTokenBalance(user.getTokenBalance() + amount);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void assignPlan(Long userId, Long planId) {
        User user = getUser(userId);
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan no encontrado: " + planId));
        user.setPlan(plan);
        // Decision del equipo: al cambiar de plan se acumulan tokens
        user.setTokenBalance(user.getTokenBalance() + plan.getTokensGranted());
        userRepository.save(user);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + id));
    }

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .name(u.getName())
                .role(u.getRole().name())
                .tokenBalance(u.getTokenBalance())
                .active(u.getActive())
                .planName(u.getPlan() != null ? u.getPlan().getName() : null)
                .build();
    }
}
