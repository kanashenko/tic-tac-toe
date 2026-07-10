package com.tictactoe.gameengine.dto;

import com.tictactoe.gameengine.domain.Game;
import com.tictactoe.gameengine.domain.GameStatus;

import java.util.List;

/**
 * Returned by every Game Engine endpoint.
 *
 * @param gameId the game identifier
 * @param board  the board as 3 rows of 3 symbols ({@code '-'} for an empty cell)
 * @param status whether the game is in progress, won, or drawn
 * @param winner the winning player's name ("X"/"O"), or {@code null} if there is none
 */
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
