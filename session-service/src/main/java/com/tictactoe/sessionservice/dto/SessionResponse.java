package com.tictactoe.sessionservice.dto;

import com.tictactoe.sessionservice.model.SessionStatus;

import java.util.List;

public record SessionResponse(
        String sessionId,
        SessionStatus status,
        String winner,
        List<List<Character>> board,
        List<MoveDto> moveHistory
) {
}