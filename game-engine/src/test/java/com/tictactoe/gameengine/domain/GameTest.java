package com.tictactoe.gameengine.domain;

import com.tictactoe.gameengine.exception.InvalidMoveException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTest {

    @Test
    void startCreatesAnEmptyBoardWithXToMoveFirst() {
        Game game = Game.start("g1");

        assertThat(game.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(game.nextTurn()).isEqualTo(Player.X);
        assertThat(game.winner()).isNull();
        assertThat(game.board()).isEqualTo(Board.empty());
    }

    @Test
    void applyMoveAlternatesTheNextTurn() {
        Game game = Game.start("g1").applyMove(Player.X, 0, 0);

        assertThat(game.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(game.nextTurn()).isEqualTo(Player.O);
        assertThat(game.board().at(0, 0)).isEqualTo('X');
    }

    @Test
    void threeInARowEndsTheGameWithAWinner() {
        Game game = Game.start("g1")
                .applyMove(Player.X, 0, 0)
                .applyMove(Player.O, 1, 0)
                .applyMove(Player.X, 0, 1)
                .applyMove(Player.O, 1, 1)
                .applyMove(Player.X, 0, 2); // top row complete for X

        assertThat(game.status()).isEqualTo(GameStatus.WIN);
        assertThat(game.winner()).isEqualTo(Player.X);
        assertThat(game.nextTurn()).isNull();
    }

    @Test
    void aFullBoardWithNoWinnerIsADraw() {
        // X | O | X
        // X | O | O
        // O | X | X
        Game game = Game.start("g1")
                .applyMove(Player.X, 0, 0)
                .applyMove(Player.O, 0, 1)
                .applyMove(Player.X, 0, 2)
                .applyMove(Player.O, 1, 1)
                .applyMove(Player.X, 1, 0)
                .applyMove(Player.O, 1, 2)
                .applyMove(Player.X, 2, 1)
                .applyMove(Player.O, 2, 0)
                .applyMove(Player.X, 2, 2);

        assertThat(game.status()).isEqualTo(GameStatus.DRAW);
        assertThat(game.winner()).isNull();
    }

    @Test
    void movingOutOfTurnIsRejected() {
        Game game = Game.start("g1");

        assertThatThrownBy(() -> game.applyMove(Player.O, 0, 0))
                .isInstanceOf(InvalidMoveException.class)
                .hasMessageContaining("turn");
    }

    @Test
    void movingOnAnOccupiedCellIsRejected() {
        Game game = Game.start("g1").applyMove(Player.X, 0, 0);

        assertThatThrownBy(() -> game.applyMove(Player.O, 0, 0))
                .isInstanceOf(InvalidMoveException.class)
                .hasMessageContaining("occupied");
    }

    @Test
    void movingAfterTheGameHasEndedIsRejected() {
        Game finished = Game.start("g1")
                .applyMove(Player.X, 0, 0)
                .applyMove(Player.O, 1, 0)
                .applyMove(Player.X, 0, 1)
                .applyMove(Player.O, 1, 1)
                .applyMove(Player.X, 0, 2);

        assertThatThrownBy(() -> finished.applyMove(Player.O, 2, 2))
                .isInstanceOf(InvalidMoveException.class)
                .hasMessageContaining("already ended");
    }
}
