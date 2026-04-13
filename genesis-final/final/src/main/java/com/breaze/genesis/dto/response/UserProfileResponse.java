package com.breaze.genesis.dto.response;

import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserProfileResponse {
    private Long   id;
    private String email;
    private String name;
    private String role;
    private Integer tokenBalance;
    private String  planName;
    private Integer planTokens;
    private Boolean active;
}
