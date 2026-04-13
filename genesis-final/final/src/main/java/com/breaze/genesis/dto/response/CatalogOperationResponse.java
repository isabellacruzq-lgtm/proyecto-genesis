package com.breaze.genesis.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogOperationResponse {

    private Long          id;
    private String        code;
    private String        name;
    private String        description;
    private Integer       baseCost;
    private Boolean       active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
