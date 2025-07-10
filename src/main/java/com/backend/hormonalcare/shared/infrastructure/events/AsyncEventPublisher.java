package com.backend.hormonalcare.shared.infrastructure.events;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Un servicio que permite publicar eventos de forma asíncrona.
 * Utiliza Spring @Async para ejecutar la publicación de eventos en hilos
 * separados,
 * lo que mejora la escalabilidad al no bloquear el hilo principal.
 */
@Component
public class AsyncEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public AsyncEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publica un evento de forma asíncrona utilizando el pool de hilos
     * "eventExecutor".
     * 
     * @param event El evento a publicar
     */
    @Async("eventExecutor")
    public void publishEvent(ApplicationEvent event) {
        eventPublisher.publishEvent(event);
    }

    /**
     * Publica un objeto como evento de forma asíncrona utilizando el pool de hilos
     * "eventExecutor".
     * 
     * @param event El objeto que se publicará como evento
     */
    @Async("eventExecutor")
    public void publishEvent(Object event) {
        eventPublisher.publishEvent(event);
    }
}
