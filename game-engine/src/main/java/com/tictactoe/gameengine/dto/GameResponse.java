package com.tictactoe.gameengine.dto;

import com.tictactoe.gameengine.domain.Game;
import com.tictactoe.gameengine.domain.GameStatus;

import java.util.List;

public record GameResponse(
        String gameId,
        List<List<Character>> board,
        GameStatus status,
        String winner
) {
    public static GameResponse from(Game game) {
        return new GameResponse(
                game.gameId(),
                game.board().asRows(),
                game.status(),
                game.winner() != null ? game.winner().name() : null
        );
    }
}
