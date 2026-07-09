package com.tictactoe.sessionservice.dto;

import com.tictactoe.sessionservice.model.SessionStatus;

import java.util.List;

/**
 * Wire representation of a session, returned by every {@code /sessions}
 * endpoint and pushed as-is over the session's WebSocket feed.
 *
 * @param sessionId   the session identifier (also the Game Engine's game ID)
 * @param status      the session's current status
 * @param winner      the winning player's symbol, or {@code null} if there is none
 * @param board       the current board as 3 rows of 3 symbols
 * @param moveHistory the moves played so far, oldest first
 */
public record SessionResponse(
        String sessionId,
        SessionStatus status,
        String winner,
        List<List<Character>> board,
        List<MoveDto> moveHistory
) {
}
