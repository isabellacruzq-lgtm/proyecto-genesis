package com.breaze.genesis.dto.response;

import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PlanResponse {
    private Long    id;
    private String  name;
    private Integer tokensGranted;
    private String  description;
    private Boolean active;
}
