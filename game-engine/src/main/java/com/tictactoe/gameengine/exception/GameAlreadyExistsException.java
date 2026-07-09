package com.tictactoe.gameengine.exception;

public class GameAlreadyExistsException extends RuntimeException {
    public GameAlreadyExistsException(String gameId) {
        super("Game already exists: " + gameId);
    }
}
