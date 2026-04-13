package com.breaze.genesis.service.operation;

import com.breaze.genesis.entity.ExchangeRate;
import com.breaze.genesis.repository.ExchangeRateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OP-02 · Conversor COP ↔ USD
 *
 * Convierte entre COP y USD usando la tasa almacenada en BD,
 * que el administrador puede actualizar en cualquier momento.
 */
@Component
@RequiredArgsConstructor
public class CurrencyConverterOperation implements Operation {

    private final ObjectMapper objectMapper;
    private final ExchangeRateRepository exchangeRateRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String getCode() {
        return "OP-02";
    }

    @Override
    public String execute(String inputJson) {
        try {
            Map<?, ?> input = objectMapper.readValue(inputJson, Map.class);

            BigDecimal monto        = new BigDecimal(input.get("monto").toString());
            String     monedaOrigen = input.get("moneda_origen").toString().toUpperCase();

            if (!monedaOrigen.equals("COP") && !monedaOrigen.equals("USD"))
                throw new OperationExecutionException("moneda_origen debe ser COP o USD");
            if (monto.compareTo(BigDecimal.ZERO) < 0)
                throw new OperationExecutionException("El monto no puede ser negativo");

            ExchangeRate rate = exchangeRateRepository.findTopByOrderByUpdatedAtDesc()
                    .orElseThrow(() -> new OperationExecutionException(
                            "No hay tasa de cambio configurada. El administrador debe registrarla primero."));

            BigDecimal tasaCopPorUsd = rate.getCopPerUsd();
            BigDecimal resultado;
            String     direccion;

            if (monedaOrigen.equals("COP")) {
                resultado = monto.divide(tasaCopPorUsd, 4, RoundingMode.HALF_UP);
                direccion = "COP → USD";
            } else {
                resultado = monto.multiply(tasaCopPorUsd).setScale(2, RoundingMode.HALF_UP);
                direccion = "USD → COP";
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("monto_original",        monto.setScale(2, RoundingMode.HALF_UP));
            result.put("moneda_origen",          monedaOrigen);
            result.put("monto_convertido",       resultado);
            result.put("moneda_destino",         monedaOrigen.equals("COP") ? "USD" : "COP");
            result.put("direccion",              direccion);
            result.put("tasa_cop_por_usd",       tasaCopPorUsd);
            result.put("tasa_actualizada_el",    rate.getUpdatedAt().format(FMT));

            return objectMapper.writeValueAsString(result);

        } catch (OperationExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new OperationExecutionException("Error en conversión de moneda: " + e.getMessage(), e);
        }
    }
}
