package com.breaze.genesis.service.operation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OP-04 · Calculadora de sueño
 *
 * Calcula horarios óptimos para dormir o despertar en base a ciclos de 90 minutos.
 * Genera 3 opciones: Mínimo (4 ciclos), Recomendado (5 ciclos), Ideal (6 ciclos).
 */
@Component
@RequiredArgsConstructor
public class SleepCalculatorOperation implements Operation {

    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int CYCLE_MINUTES = 90;

    @Override
    public String getCode() {
        return "OP-04";
    }

    @Override
    public String execute(String inputJson) {
        try {
            Map<?, ?> input = objectMapper.readValue(inputJson, Map.class);

            String modo = input.get("modo").toString().toUpperCase();
            String horaStr = input.get("hora").toString();
            int minParaDormir = input.containsKey("minutos_para_dormir")
                    ? Integer.parseInt(input.get("minutos_para_dormir").toString())
                    : 14;

            if (!modo.equals("HORA_DESPERTAR") && !modo.equals("HORA_ACOSTARSE"))
                throw new OperationExecutionException("modo debe ser HORA_DESPERTAR o HORA_ACOSTARSE");
            if (minParaDormir < 0)
                throw new OperationExecutionException("minutos_para_dormir no puede ser negativo");

            LocalTime horaReferencia = LocalTime.parse(horaStr, TIME_FMT);

            // Ciclos: 4 (Mínimo), 5 (Recomendado), 6 (Ideal)
            int[]    ciclos   = {4, 5, 6};
            String[] etiquetas = {"Mínimo", "Recomendado", "Ideal"};

            List<Map<String, Object>> opciones = new ArrayList<>();

            for (int idx = 0; idx < ciclos.length; idx++) {
                int ciclo = ciclos[idx];
                int totalMinutosSueno = ciclo * CYCLE_MINUTES;
                LocalTime horaCalculada;

                if (modo.equals("HORA_DESPERTAR")) {
                    // hora_acostarse = hora_despertar - (ciclos × 90) - minutos_para_dormir
                    horaCalculada = horaReferencia
                            .minusMinutes(totalMinutosSueno)
                            .minusMinutes(minParaDormir);
                } else {
                    // hora_despertar = hora_acostarse + minutos_para_dormir + (ciclos × 90)
                    horaCalculada = horaReferencia
                            .plusMinutes(minParaDormir)
                            .plusMinutes(totalMinutosSueno);
                }

                double horasTotales = totalMinutosSueno / 60.0;

                Map<String, Object> opcion = new LinkedHashMap<>();
                opcion.put("etiqueta",         etiquetas[idx]);
                opcion.put("ciclos",            ciclo);
                opcion.put("horas_de_sueno",    horasTotales);
                opcion.put(modo.equals("HORA_DESPERTAR") ? "hora_acostarse" : "hora_despertar",
                           horaCalculada.format(TIME_FMT));
                opciones.add(opcion);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("modo",                modo);
            result.put("hora_referencia",     horaStr);
            result.put("minutos_para_dormir", minParaDormir);
            result.put("opciones",            opciones);

            return objectMapper.writeValueAsString(result);

        } catch (OperationExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new OperationExecutionException("Error al calcular el horario de sueño: " + e.getMessage(), e);
        }
    }
}
