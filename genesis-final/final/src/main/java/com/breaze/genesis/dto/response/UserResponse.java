package com.breaze.genesis.dto.response;

import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserResponse {
    private Long    id;
    private String  email;
    private String  name;
    private String  role;
    private Integer tokenBalance;
    private Boolean active;
    private String  planName;
}
