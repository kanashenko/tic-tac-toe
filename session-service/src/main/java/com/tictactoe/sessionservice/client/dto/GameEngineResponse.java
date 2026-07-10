package com.tictactoe.sessionservice.client.dto;

import java.util.List;

/** Mirrors the Game Engine's {@code GameResponse} JSON shape. */
public record GameEngineResponse(
        String gameId,
        List<List<Character>> board,
        GameEngineStatus status,
        String winner
) {
}
