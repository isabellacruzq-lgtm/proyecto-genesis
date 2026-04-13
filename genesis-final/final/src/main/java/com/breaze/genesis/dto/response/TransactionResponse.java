package com.breaze.genesis.dto.response;

import com.breaze.genesis.entity.Transaction.TransactionStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long              id;
    private String            operationCode;
    private String            operationName;
    private Integer           tokensInput;
    private Integer           tokensOutput;
    private Integer           baseCost;
    private Integer           totalCost;
    private Integer           balanceBefore;
    private Integer           balanceAfter;
    private TransactionStatus status;
    private String            errorMessage;
    private LocalDateTime     executedAt;
}
