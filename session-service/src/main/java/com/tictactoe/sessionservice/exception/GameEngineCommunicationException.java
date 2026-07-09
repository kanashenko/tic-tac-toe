package com.tictactoe.sessionservice.exception;

import lombok.Getter;

/** Thrown by {@link com.tictactoe.sessionservice.client.GameEngineClient} when the Game Engine returns a non-2xx response. */
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
