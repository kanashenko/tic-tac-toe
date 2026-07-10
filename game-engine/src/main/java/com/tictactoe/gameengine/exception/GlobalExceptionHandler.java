package com.tictactoe.gameengine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Maps domain and validation exceptions to RFC 7807 {@link ProblemDetail}
 * responses, so callers get a machine-readable status/detail pair instead of
 * a generic 500 for expected error conditions (unknown game, illegal move,
 * duplicate creation, malformed request body). Framework-level client errors
 * are rendered as ProblemDetail too, via {@code spring.mvc.problemdetails.enabled}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GameNotFoundException.class)
    public ProblemDetail handleNotFound(GameNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(GameAlreadyExistsException.class)
    public ProblemDetail handleAlreadyExists(GameAlreadyExistsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidMoveException.class)
    public ProblemDetail handleInvalidMove(InvalidMoveException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }
}
