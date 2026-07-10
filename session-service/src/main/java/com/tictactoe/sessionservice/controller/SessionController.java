package com.tictactoe.sessionservice.controller;

import com.tictactoe.sessionservice.dto.SessionResponse;
import com.tictactoe.sessionservice.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST API for the Session Service: create sessions, kick off the automated
 * simulation, and read back session state and move history. Live updates
 * while a simulation runs are pushed separately over
 * {@code /ws/sessions?sessionId=...} — see
 * {@link com.tictactoe.sessionservice.websocket.SessionWebSocketHandler}.
 */
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    /** Creates a session; its game is initialized lazily when simulation starts. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SessionResponse> createSession() {
        return sessionService.createSession();
    }

    /**
     * Starts the automated play-out of a session in the background and
     * returns immediately; progress is delivered over the session's
     * WebSocket feed rather than in this response. Returns 404 if the session
     * does not exist.
     */
    @PostMapping("/{sessionId}/simulate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> simulate(@PathVariable String sessionId) {
        return sessionService.startSimulation(sessionId);
    }

    /** Returns the session's current status plus its full move history so far. */
    @GetMapping("/{sessionId}")
    public Mono<SessionResponse> getSession(@PathVariable String sessionId) {
        return sessionService.getSession(sessionId);
    }
}
