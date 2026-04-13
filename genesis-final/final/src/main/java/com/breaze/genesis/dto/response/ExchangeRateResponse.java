package com.breaze.genesis.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ExchangeRateResponse {
    private Long         id;
    private BigDecimal   copPerUsd;
    private LocalDateTime updatedAt;
    private String       updatedBy;
}
