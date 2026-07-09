package com.tictactoe.e2e;

import java.util.List;

/**
 * Mirrors {@code com.tictactoe.sessionservice.dto.SessionResponse} — the JSON
 * shape returned by {@code GET/POST /sessions/**} and pushed to subscribers of
 * {@code /ws/sessions}.
 */
public record SessionResponse(
        String sessionId,
        SessionStatus status,
        String winner,
        List<List<Character>> board,
        List<MoveDto> moveHistory
) {
}
