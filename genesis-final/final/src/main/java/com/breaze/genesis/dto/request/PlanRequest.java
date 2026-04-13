package com.breaze.genesis.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlanRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotNull(message = "Los tokens otorgados son obligatorios")
    @Min(value = 1, message = "Los tokens deben ser al menos 1")
    private Integer tokensGranted;

    private String description;
}
