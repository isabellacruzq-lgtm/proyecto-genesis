package com.breaze.genesis.service.operation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OP-03 · Calculadora de IMC
 *
 * Calcula el Índice de Masa Corporal, su categoría y el rango de peso saludable.
 */
@Component
@RequiredArgsConstructor
public class BmiCalculatorOperation implements Operation {

    private final ObjectMapper objectMapper;

    @Override
    public String getCode() {
        return "OP-03";
    }

    @Override
    public String execute(String inputJson) {
        try {
            Map<?, ?> input = objectMapper.readValue(inputJson, Map.class);

            BigDecimal pesoKg   = new BigDecimal(input.get("peso_kg").toString());
            BigDecimal alturaCm = new BigDecimal(input.get("altura_cm").toString());

            if (pesoKg.compareTo(BigDecimal.ZERO) <= 0)
                throw new OperationExecutionException("El peso debe ser mayor a 0");
            if (alturaCm.compareTo(BigDecimal.ZERO) <= 0)
                throw new OperationExecutionException("La altura debe ser mayor a 0");

            // altura_m = altura_cm / 100
            BigDecimal alturaM = alturaCm.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

            // IMC = peso_kg / altura_m²
            BigDecimal alturaM2 = alturaM.multiply(alturaM);
            BigDecimal imc      = pesoKg.divide(alturaM2, 6, RoundingMode.HALF_UP);
            BigDecimal imcRedondeado = imc.setScale(2, RoundingMode.HALF_UP);

            String categoria = clasificarImc(imc);

            // Rango de peso saludable
            BigDecimal pesoMin = BigDecimal.valueOf(18.5).multiply(alturaM2).setScale(2, RoundingMode.HALF_UP);
            BigDecimal pesoMax = BigDecimal.valueOf(24.9).multiply(alturaM2).setScale(2, RoundingMode.HALF_UP);

            // Diferencia con el rango saludable
            String diferencia;
            BigDecimal difValor;
            if (pesoKg.compareTo(pesoMin) < 0) {
                difValor = pesoMin.subtract(pesoKg).setScale(2, RoundingMode.HALF_UP);
                diferencia = "Te faltan " + difValor + " kg para alcanzar el peso mínimo saludable";
            } else if (pesoKg.compareTo(pesoMax) > 0) {
                difValor = pesoKg.subtract(pesoMax).setScale(2, RoundingMode.HALF_UP);
                diferencia = "Superas en " + difValor + " kg el peso máximo saludable";
            } else {
                diferencia = "Tu peso está dentro del rango saludable";
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("imc",               imcRedondeado);
            result.put("categoria",          categoria);
            result.put("peso_min_saludable", pesoMin);
            result.put("peso_max_saludable", pesoMax);
            result.put("diferencia",         diferencia);

            return objectMapper.writeValueAsString(result);

        } catch (OperationExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new OperationExecutionException("Error al calcular el IMC: " + e.getMessage(), e);
        }
    }

    private String clasificarImc(BigDecimal imc) {
        double val = imc.doubleValue();
        if (val < 18.5)  return "Bajo peso";
        if (val < 25.0)  return "Peso normal";
        if (val < 30.0)  return "Sobrepeso";
        return "Obesidad";
    }
}
