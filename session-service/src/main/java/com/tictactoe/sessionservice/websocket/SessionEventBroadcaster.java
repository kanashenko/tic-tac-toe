package com.tictactoe.sessionservice.websocket;

import com.tictactoe.sessionservice.dto.SessionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SessionEventBroadcaster {

    private final ConcurrentHashMap<String, Sinks.Many<SessionResponse>> sinks = new ConcurrentHashMap<>();

    public Flux<SessionResponse> subscribe(String sessionId) {
        log.info("--- Subscriber attaching to sink for game: {}", sessionId);
        return sinkFor(sessionId).asFlux();
    }

    public void publish(String sessionId, SessionResponse event) {
        log.info("+++ Publishing event to sink for game: {} (Status: {})", sessionId, event.status());
        Sinks.EmitResult result = sinkFor(sessionId).tryEmitNext(event);
        if (result.isFailure()) {
            log.warn("Dropped event for session {}: {}", sessionId, result);
        }
    }

    public void complete(String sessionId) {
        log.info("!!! Completing and removing sink for game: {}", sessionId);
        Sinks.Many<SessionResponse> sink = sinks.remove(sessionId);
        if (sink != null) {
            sink.tryEmitComplete();
        }
    }

    private Sinks.Many<SessionResponse> sinkFor(String sessionId) {
        return sinks.computeIfAbsent(sessionId, id -> {
            log.info("*** CREATING NEW SINK for game: {}", id);
            return Sinks.many().multicast().onBackpressureBuffer();
        });
    }
}