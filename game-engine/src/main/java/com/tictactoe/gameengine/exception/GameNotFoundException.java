package com.tictactoe.gameengine.exception;

/** Thrown when a game ID referenced by a request does not exist. */
public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(String gameId) {
        super("Game not found: " + gameId);
    }
}
