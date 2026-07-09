package com.tictactoe.sessionservice.exception;

/** Thrown when a session ID referenced by a request does not exist. */
public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(String sessionId) {
        super("Session not found: " + sessionId);
    }

    public SessionNotFoundException(String sessionId, Throwable cause) {
        super("Session not found: " + sessionId, cause);
    }
}
