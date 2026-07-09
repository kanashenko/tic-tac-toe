package com.tictactoe.e2e;

/**
 * Mirrors {@code com.tictactoe.sessionservice.model.SessionStatus}.
 *
 * <p>The e2e-tests module intentionally does not depend on session-service's
 * classes — it only talks to the running containers over HTTP/WebSocket, the
 * same way a real client would. This local copy keeps the test a genuine
 * black-box contract test instead of a compile-time coupling to internal
 * server types.
 */
public enum SessionStatus {
    IN_PROGRESS, WIN, DRAW, ERROR
}
