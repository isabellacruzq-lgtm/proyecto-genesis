package com.breaze.genesis.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsResponse {

    private List<DailyTokens>    tokensByDay;
    private List<OperationStat>  mostExecutedOperations;
    private List<UserConsumption> topConsumers;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DailyTokens {
        private String  day;
        private Long    totalTokens;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OperationStat {
        private String operationCode;
        private String operationName;
        private Long   executions;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserConsumption {
        private Long   userId;
        private String userName;
        private String userEmail;
        private Long   totalTokensConsumed;
    }
}
