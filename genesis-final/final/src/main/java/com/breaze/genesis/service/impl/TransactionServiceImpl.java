package com.breaze.genesis.service.impl;

import com.breaze.genesis.dto.response.MetricsResponse;
import com.breaze.genesis.dto.response.TransactionResponse;
import com.breaze.genesis.dto.response.UserProfileResponse;
import com.breaze.genesis.entity.User;
import com.breaze.genesis.repository.TransactionRepository;
import com.breaze.genesis.repository.UserRepository;
import com.breaze.genesis.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository        userRepository;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserHistory(Long userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable)
                .map(tx -> TransactionResponse.builder()
                        .id(tx.getId())
                        .operationCode(tx.getOperation().getCode())
                        .operationName(tx.getOperation().getName())
                        .tokensInput(tx.getTokensInput())
                        .tokensOutput(tx.getTokensOutput())
                        .baseCost(tx.getBaseCost())
                        .totalCost(tx.getTotalCost())
                        .balanceBefore(tx.getBalanceBefore())
                        .balanceAfter(tx.getBalanceAfter())
                        .status(tx.getStatus())
                        .errorMessage(tx.getErrorMessage())
                        .executedAt(tx.getCreatedAt())
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public MetricsResponse getGlobalMetrics(String from, String to, int topN) {
        LocalDateTime fromDt = (from != null)
                ? LocalDate.parse(from, DATE_FMT).atStartOfDay()
                : LocalDateTime.now().minusDays(30);
        LocalDateTime toDt = (to != null)
                ? LocalDate.parse(to, DATE_FMT).atTime(23, 59, 59)
                : LocalDateTime.now();

        List<MetricsResponse.DailyTokens> tokensByDay = transactionRepository
                .findTokensConsumedPerDay(fromDt, toDt).stream()
                .map(row -> MetricsResponse.DailyTokens.builder()
                        .day(row[0].toString())
                        .totalTokens(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());

        List<MetricsResponse.OperationStat> mostExecuted = transactionRepository
                .findMostExecutedOperations(PageRequest.of(0, topN)).stream()
                .map(row -> MetricsResponse.OperationStat.builder()
                        .operationCode(row[0].toString())
                        .operationName(row[1].toString())
                        .executions(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());

        List<MetricsResponse.UserConsumption> topConsumers = transactionRepository
                .findTopConsumers(PageRequest.of(0, topN)).stream()
                .map(row -> MetricsResponse.UserConsumption.builder()
                        .userId(((Number) row[0]).longValue())
                        .userName(row[1].toString())
                        .userEmail(row[2].toString())
                        .totalTokensConsumed(((Number) row[3]).longValue())
                        .build())
                .collect(Collectors.toList());

        return MetricsResponse.builder()
                .tokensByDay(tokensByDay)
                .mostExecutedOperations(mostExecuted)
                .topConsumers(topConsumers)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .tokenBalance(user.getTokenBalance())
                .planName(user.getPlan() != null ? user.getPlan().getName() : null)
                .planTokens(user.getPlan() != null ? user.getPlan().getTokensGranted() : null)
                .active(user.getActive())
                .build();
    }
}
