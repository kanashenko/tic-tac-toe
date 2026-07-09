package com.tictactoe.sessionservice.exception;

import lombok.Getter;

@Getter
public class GameEngineCommunicationException extends RuntimeException {
    private final int statusCode;

    public GameEngineCommunicationException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public GameEngineCommunicationException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}