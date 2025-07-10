package com.backend.hormonalcare.shared.infrastructure.config;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

/**
 * Indicador de salud que verifica la disponibilidad de servicios externos
 * como Supabase Storage.
 * 
 * Este indicador está diseñado para mostrar el estado de los servicios externos
 * sin afectar el estado general de la aplicación.
 */
@Component("externalServices")
public class ExternalServicesHealthIndicator implements HealthIndicator {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucket;

    @Override
    public Health health() {
        try {
            // Verificamos la conectividad con Supabase Storage
            boolean supabaseAvailable = isSupabaseStorageAvailable();

            // Determinar el estado correcto
            Status status = supabaseAvailable ? Status.UP : new Status("EXTERNAL_SERVICES_DOWN", "DOWN");

            // Creamos un builder de salud con el estado apropiado
            Health.Builder builder = Health.status(status);

            // Agregamos información detallada sobre Supabase
            builder.withDetail("supabase.storage.status", supabaseAvailable ? "CONNECTED" : "DISCONNECTED")
                    .withDetail("supabase.storage.url", supabaseUrl)
                    .withDetail("supabase.storage.bucket", bucket)
                    .withDetail("type", "EXTERNAL_SERVICE");

            if (supabaseAvailable) {
                builder.withDetail("message", "Los servicios externos están funcionando correctamente");
            } else {
                builder.withDetail("message",
                        "Supabase Storage no está disponible - Algunas funcionalidades de carga de archivos pueden verse afectadas");
            }

            return builder.build();
        } catch (Exception e) {
            return Health.status(new Status("EXTERNAL_SERVICES_DOWN", "DOWN"))
                    .withDetail("error", e.getMessage())
                    .withDetail("exception", e.getClass().getName())
                    .withDetail("supabase.storage.status", "ERROR")
                    .withDetail("type", "EXTERNAL_SERVICE")
                    .withDetail("message", "Error al verificar servicios externos")
                    .build();
        }
    }

    /**
     * Verifica si el servicio Supabase Storage está disponible.
     * Realiza una solicitud GET al endpoint de health o una solicitud a la API de
     * storage
     * para comprobar conectividad.
     * 
     * @return true si el servicio está disponible, false en caso contrario
     */
    private boolean isSupabaseStorageAvailable() {
        try {
            // Utilizamos OkHttp para hacer una solicitud liviana a Supabase
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            // Primero intentamos con el endpoint de health
            Request healthRequest = new Request.Builder()
                    .url(supabaseUrl + "/health")
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .get()
                    .build();

            try (Response healthResponse = client.newCall(healthRequest).execute()) {
                if (healthResponse.isSuccessful()) {
                    return true;
                }
            }

            // Si llegamos aquí, intentamos con el endpoint de storage
            Request storageRequest = new Request.Builder()
                    .url(supabaseUrl + "/storage/v1/bucket")
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .get()
                    .build();

            try (Response storageResponse = client.newCall(storageRequest).execute()) {
                return storageResponse.isSuccessful(); // Código 2xx indica éxito
            }

        } catch (Exception e) {
            // Si hay cualquier error, consideramos que el servicio no está disponible
            return false;
        }
    }
}
