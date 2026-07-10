package com.tictactoe.gameengine.exception;

/** Thrown when {@code POST /games/{gameId}} is called with an ID that's already in use. */
public class GameAlreadyExistsException extends RuntimeException {
    public GameAlreadyExistsException(String gameId) {
        super("Game already exists: " + gameId);
    }
}
