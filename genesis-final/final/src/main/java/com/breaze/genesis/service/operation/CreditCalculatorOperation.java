package com.breaze.genesis.service.operation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OP-01 · ¿Cuánto me cuesta ese crédito?
 *
 * Calcula la cuota mensual, total pagado, intereses totales y la tabla
 * de amortización completa usando la fórmula francesa de cuota fija.
 */
@Component
@RequiredArgsConstructor
public class CreditCalculatorOperation implements Operation {

    private final ObjectMapper objectMapper;
    private static final MathContext MC = new MathContext(15, RoundingMode.HALF_UP);

    @Override
    public String getCode() {
        return "OP-01";
    }

    @Override
    public String execute(String inputJson) {
        try {
            Map<?, ?> input = objectMapper.readValue(inputJson, Map.class);

            BigDecimal precio      = new BigDecimal(input.get("precio").toString());
            int        cuotas      = Integer.parseInt(input.get("cuotas").toString());
            BigDecimal tasaMensual = new BigDecimal(input.get("tasa_mensual").toString());

            if (precio.compareTo(BigDecimal.ZERO) <= 0)
                throw new OperationExecutionException("El precio debe ser mayor a 0");
            if (cuotas <= 0)
                throw new OperationExecutionException("El número de cuotas debe ser mayor a 0");
            if (tasaMensual.compareTo(BigDecimal.ZERO) < 0)
                throw new OperationExecutionException("La tasa mensual no puede ser negativa");

            // i = tasa_mensual / 100
            BigDecimal i = tasaMensual.divide(BigDecimal.valueOf(100), MC);

            BigDecimal cuota;
            if (i.compareTo(BigDecimal.ZERO) == 0) {
                // Sin interés: cuota simple
                cuota = precio.divide(BigDecimal.valueOf(cuotas), MC);
            } else {
                // cuota = precio × (i × (1+i)^cuotas) / ((1+i)^cuotas - 1)
                BigDecimal unoPlusI     = BigDecimal.ONE.add(i, MC);
                BigDecimal unoPlusIPow  = unoPlusI.pow(cuotas, MC);
                BigDecimal numerador    = precio.multiply(i.multiply(unoPlusIPow, MC), MC);
                BigDecimal denominador  = unoPlusIPow.subtract(BigDecimal.ONE, MC);
                cuota = numerador.divide(denominador, MC);
            }

            BigDecimal totalPagado   = cuota.multiply(BigDecimal.valueOf(cuotas), MC);
            BigDecimal totalIntereses = totalPagado.subtract(precio, MC);

            // Tabla de amortización
            List<Map<String, Object>> tabla = new ArrayList<>();
            BigDecimal saldo = precio;

            for (int periodo = 1; periodo <= cuotas; periodo++) {
                BigDecimal interesPagado    = saldo.multiply(i, MC);
                BigDecimal capitalAmortizado = cuota.subtract(interesPagado, MC);
                saldo = saldo.subtract(capitalAmortizado, MC);

                // Corrección de último período por redondeo
                if (periodo == cuotas) saldo = BigDecimal.ZERO;

                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("periodo",             periodo);
                fila.put("cuota",               round2(cuota));
                fila.put("interes_pagado",       round2(interesPagado));
                fila.put("capital_amortizado",   round2(capitalAmortizado));
                fila.put("saldo_restante",       round2(saldo.max(BigDecimal.ZERO)));
                tabla.add(fila);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("cuota_mensual",   round2(cuota));
            result.put("total_pagado",    round2(totalPagado));
            result.put("total_intereses", round2(totalIntereses));
            result.put("tabla_amortizacion", tabla);

            return objectMapper.writeValueAsString(result);

        } catch (OperationExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new OperationExecutionException("Error al calcular el crédito: " + e.getMessage(), e);
        }
    }

    private BigDecimal round2(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
