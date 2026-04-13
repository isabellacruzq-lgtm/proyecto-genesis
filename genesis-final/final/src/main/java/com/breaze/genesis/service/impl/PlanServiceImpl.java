package com.breaze.genesis.service.impl;

import com.breaze.genesis.dto.request.PlanRequest;
import com.breaze.genesis.dto.response.PlanResponse;
import com.breaze.genesis.entity.Plan;
import com.breaze.genesis.entity.User;
import com.breaze.genesis.repository.PlanRepository;
import com.breaze.genesis.repository.UserRepository;
import com.breaze.genesis.service.PlanService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PlanResponse create(PlanRequest request) {
        if (planRepository.findByName(request.getName()).isPresent())
            throw new IllegalArgumentException("Ya existe un plan con el nombre: " + request.getName());
        Plan plan = Plan.builder()
                .name(request.getName())
                .tokensGranted(request.getTokensGranted())
                .description(request.getDescription())
                .active(true)
                .build();
        return toResponse(planRepository.save(plan));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlanResponse> findAllPaged(Pageable pageable) {
        return planRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PlanResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    @Transactional
    public PlanResponse update(Long id, PlanRequest request) {
        Plan plan = getOrThrow(id);
        plan.setName(request.getName());
        plan.setTokensGranted(request.getTokensGranted());
        plan.setDescription(request.getDescription());
        return toResponse(planRepository.save(plan));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Plan plan = getOrThrow(id);
        boolean tieneUsuarios = userRepository.existsByPlanId(id);
        if (tieneUsuarios)
            throw new IllegalStateException("No se puede eliminar un plan con suscripciones activas");
        planRepository.delete(plan);
    }

    @Override
    @Transactional
    public void subscribe(Long planId, String userEmail) {
        Plan plan = getOrThrow(planId);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        user.setPlan(plan);
        // Al suscribirse se acreditan los tokens del plan
        user.setTokenBalance(user.getTokenBalance() + plan.getTokensGranted());
        userRepository.save(user);
    }

    private Plan getOrThrow(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Plan no encontrado: " + id));
    }

    private PlanResponse toResponse(Plan p) {
        return PlanResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .tokensGranted(p.getTokensGranted())
                .description(p.getDescription())
                .active(p.getActive())
                .build();
    }
}
