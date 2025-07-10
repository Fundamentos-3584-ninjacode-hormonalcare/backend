package com.backend.hormonalcare.shared.infrastructure.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

/**
 * Indicador de salud principal que siempre reporta UP mientras la aplicación
 * esté en ejecución.
 * Este indicador garantiza que el estado general de la aplicación no se vea
 * afectado
 * por servicios externos no críticos.
 */
@Component("appHealth")
public class ApplicationHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up()
                .withDetail("message", "La aplicación Hormonal Care está en ejecución")
                .withDetail("status", Status.UP.getCode())
                .build();
    }
}
