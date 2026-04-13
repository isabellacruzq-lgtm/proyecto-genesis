package com.breaze.genesis.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteOperationResponse {

    private String          operationCode;
    private String          operationName;
    private Map<String, Object> result;      // resultado parseado de la operación
    private Integer         tokensInput;
    private Integer         tokensOutput;
    private Integer         baseCost;
    private Integer         totalCost;
    private Integer         balanceBefore;
    private Integer         balanceAfter;
    private LocalDateTime   executedAt;
}
