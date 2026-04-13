package com.breaze.genesis.service;

import org.springframework.stereotype.Component;

/**
 * Responsabilidad única: calcular el costo en tokens de una operación.
 *
 * PRINCIPIO SRP: esta clase solo sabe calcular tokens.
 * No sabe nada de usuarios, planes, base de datos ni HTTP.
 *
 * Fórmulas:
 *   tokens_entrada = floor(longitud del JSON de entrada / 4)
 *   tokens_salida  = floor(longitud del JSON de salida / 4)
 *   costo_total    = costo_base + tokens_entrada + tokens_salida
 */
@Component
public class TokenCalculator {

    public int calculateInputTokens(String inputJson) {
        return inputJson == null ? 0 : inputJson.length() / 4;
    }

    public int calculateOutputTokens(String outputJson) {
        return outputJson == null ? 0 : outputJson.length() / 4;
    }

    public int calculateTotal(int baseCost, int tokensInput, int tokensOutput) {
        return baseCost + tokensInput + tokensOutput;
    }

    /**
     * Método conveniente para calcular el total completo de una operación.
     */
    public int calculateTotal(int baseCost, String inputJson, String outputJson) {
        return calculateTotal(
                baseCost,
                calculateInputTokens(inputJson),
                calculateOutputTokens(outputJson)
        );
    }
}
