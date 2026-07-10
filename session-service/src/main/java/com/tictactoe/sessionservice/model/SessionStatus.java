package com.tictactoe.sessionservice.model;

/**
 * The lifecycle states of a session. {@code ERROR} is session-specific — it
 * covers a simulation that failed unrecoverably (e.g. the Game Engine became
 * unreachable), something the Game Engine's own status never reports.
 */
public enum SessionStatus {
    IN_PROGRESS, WIN, DRAW, ERROR
}
