package com.tictactoe.sessionservice.client.dto;

import java.util.List;

public record GameEngineResponse(
        String gameId,
        List<List<Character>> board,
        GameEngineStatus status,
        String winner
) {
}