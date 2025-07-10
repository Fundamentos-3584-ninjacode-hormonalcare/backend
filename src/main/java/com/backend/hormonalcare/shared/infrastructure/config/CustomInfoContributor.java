package com.backend.hormonalcare.shared.infrastructure.config;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Contribuidor personalizado para el endpoint /actuator/info
 * Añade información adicional sobre el entorno de ejecución
 */
@Component
public class CustomInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> details = new HashMap<>();

        // Información del sistema
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("java.version", System.getProperty("java.version"));
        systemInfo.put("java.vendor", System.getProperty("java.vendor"));
        systemInfo.put("os.name", System.getProperty("os.name"));
        systemInfo.put("os.version", System.getProperty("os.version"));
        systemInfo.put("os.arch", System.getProperty("os.arch"));
        systemInfo.put("user.timezone", System.getProperty("user.timezone"));

        // Información de memoria
        Map<String, Object> memoryInfo = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        memoryInfo.put("total", runtime.totalMemory());
        memoryInfo.put("free", runtime.freeMemory());
        memoryInfo.put("processors", runtime.availableProcessors());

        details.put("system", systemInfo);
        details.put("memory", memoryInfo);

        builder.withDetail("environment", details);
    }
}
