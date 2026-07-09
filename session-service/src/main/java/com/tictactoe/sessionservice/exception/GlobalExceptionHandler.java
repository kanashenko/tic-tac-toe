package com.tictactoe.sessionservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.stream.Collectors;

/**
 * Maps domain and validation exceptions to RFC 7807 {@link ProblemDetail}
 * responses: an unknown session becomes 404, a Game Engine failure becomes
 * 502 (this service acting as a gateway to it), and a malformed request body
 * becomes 400.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SessionNotFoundException.class)
    public ProblemDetail handleNotFound(SessionNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(GameEngineCommunicationException.class)
    public ProblemDetail handleEngineError(GameEngineCommunicationException ex) {
        // Upstream failure (not a client mistake), so log it — a 502 with no
        // trace of why is a dead end when debugging a flaky Game Engine.
        log.warn("Game Engine returned {}: {}", ex.getStatusCode(), ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
                "Game Engine communication failed: " + ex.getMessage());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail handleValidation(WebExchangeBindException ex) {
        String detail = ex.getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }
}
