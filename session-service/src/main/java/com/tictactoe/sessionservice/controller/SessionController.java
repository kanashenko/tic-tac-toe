package com.tictactoe.sessionservice.controller;

import com.tictactoe.sessionservice.dto.SessionResponse;
import com.tictactoe.sessionservice.service.SessionService;
import com.tictactoe.sessionservice.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final SimulationService simulationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SessionResponse> createSession() {
        return sessionService.createSession();
    }

    @PostMapping("/{sessionId}/simulate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> simulate(@PathVariable String sessionId) {
        return Mono.fromRunnable(() -> simulationService.simulate(sessionId));
    }

    @GetMapping("/{sessionId}")
    public Mono<SessionResponse> getSession(@PathVariable String sessionId) {
        return sessionService.getSession(sessionId);
    }
}