package com.tictactoe.gameengine.exception;

/** Thrown by {@link com.tictactoe.gameengine.domain.Game#applyMove} when a move breaks the rules. */
public class InvalidMoveException extends RuntimeException {
    public InvalidMoveException(String message) {
        super(message);
    }
}
