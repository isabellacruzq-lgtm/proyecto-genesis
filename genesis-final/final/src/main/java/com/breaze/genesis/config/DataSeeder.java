package com.breaze.genesis.config;

import com.breaze.genesis.entity.*;
import com.breaze.genesis.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final PlanRepository             planRepository;
    private final CatalogOperationRepository catalogOperationRepository;
    private final ExchangeRateRepository     exchangeRateRepository;
    private final UserRepository             userRepository;
    private final PasswordEncoder            passwordEncoder;

    @Override
    public void run(String... args) {
        seedPlans();
        seedOperations();
        seedExchangeRate();
        seedAdminUser();
    }

    private void seedPlans() {
        if (planRepository.count() > 0) return;
        List.of(
            new Object[]{"Free",       200,  "Plan gratuito con 200 tokens"},
            new Object[]{"Pro",        1000, "Plan profesional con 1.000 tokens"},
            new Object[]{"Enterprise", 5000, "Plan empresarial con 5.000 tokens"}
        ).forEach(p -> planRepository.save(Plan.builder()
                .name((String) p[0])
                .tokensGranted((Integer) p[1])
                .description((String) p[2])
                .active(true).build()));
        log.info("Planes base creados.");
    }

    private void seedOperations() {
        if (catalogOperationRepository.count() > 0) return;
        List.of(
            new Object[]{"OP-01", "¿Cuánto me cuesta ese crédito?",  "Calcula cuota, intereses y tabla de amortización", 50},
            new Object[]{"OP-02", "Conversor COP ↔ USD",             "Convierte entre pesos colombianos y dólares",      20},
            new Object[]{"OP-03", "Calculadora de IMC",              "Calcula el IMC, categoría y rango saludable",      15},
            new Object[]{"OP-04", "Calculadora de sueño",            "Sugiere horarios óptimos para dormir",             20}
        ).forEach(o -> catalogOperationRepository.save(CatalogOperation.builder()
                .code((String) o[0])
                .name((String) o[1])
                .description((String) o[2])
                .baseCost((Integer) o[3])
                .active(true).build()));
        log.info("Operaciones del catálogo creadas.");
    }

    private void seedExchangeRate() {
        if (exchangeRateRepository.count() > 0) return;
        exchangeRateRepository.save(ExchangeRate.builder()
                .copPerUsd(new BigDecimal("4200.00"))
                .build());
        log.info("Tasa COP/USD inicial registrada: 4200.");
    }

    private void seedAdminUser() {
        String email = "admin@breaze.com";
        if (userRepository.existsByEmail(email)) return;
        userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode("Admin123!"))
                .name("Administrador Genesis")
                .role(User.Role.ADMIN)
                .tokenBalance(0)
                .active(true)
                .build());
        log.info("Admin creado → {} / Admin123!", email);
    }
}
