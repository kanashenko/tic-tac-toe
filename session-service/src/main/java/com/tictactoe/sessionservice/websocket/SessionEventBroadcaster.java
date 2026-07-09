package com.tictactoe.sessionservice.websocket;

import com.tictactoe.sessionservice.dto.SessionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Fans out {@link SessionResponse} events from {@link com.tictactoe.sessionservice.service.SimulationService}
 * to whichever WebSocket session is watching a given game, via one
 * {@link Sinks.Many} per session ID. Sinks are created lazily on first
 * {@link #subscribe} or {@link #publish} (whichever happens first) and torn
 * down once {@link #complete} is called, so neither side has to coordinate
 * startup order.
 */
@Slf4j
@Component
public class SessionEventBroadcaster {

    private final ConcurrentHashMap<String, Sinks.Many<SessionResponse>> sinks = new ConcurrentHashMap<>();

    /** Returns the live event stream for a session, creating it if this is the first subscriber. */
    public Flux<SessionResponse> subscribe(String sessionId) {
        log.debug("WebSocket subscriber attached for session {}", sessionId);
        return sinkFor(sessionId).asFlux();
    }

    /** Publishes an event to a session's stream, creating the sink if nothing has subscribed yet. */
    public void publish(String sessionId, SessionResponse event) {
        log.debug("Broadcasting {} event for session {}", event.status(), sessionId);
        Sinks.EmitResult result = sinkFor(sessionId).tryEmitNext(event);
        if (result.isFailure()) {
            log.warn("Dropped {} event for session {}: {}", event.status(), sessionId, result);
        }
    }

    /** Completes and discards a session's sink — called once its game reaches a terminal status. */
    public void complete(String sessionId) {
        log.debug("Closing event stream for session {}", sessionId);
        Sinks.Many<SessionResponse> sink = sinks.remove(sessionId);
        if (sink != null) {
            sink.tryEmitComplete();
        }
    }

    private Sinks.Many<SessionResponse> sinkFor(String sessionId) {
        return sinks.computeIfAbsent(sessionId, id -> {
            log.debug("Creating event stream for session {}", id);
            return Sinks.many().multicast().onBackpressureBuffer();
        });
    }
}
