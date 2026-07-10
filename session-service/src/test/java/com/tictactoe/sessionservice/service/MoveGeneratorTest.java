package com.tictactoe.sessionservice.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoveGeneratorTest {

    private final MoveGenerator moveGenerator = new MoveGenerator();

    @Test
    void picksOneOfTheEmptyCells() {
        List<List<Character>> board = List.of(
                List.of('X', '-', 'X'),
                List.of('-', 'O', '-'),
                List.of('X', 'O', '-')
        );

        MoveGenerator.Cell move = moveGenerator.nextMove(board);

        assertThat(board.get(move.row()).get(move.col())).isEqualTo('-');
    }

    @Test
    void theOnlyRemainingCellIsAlwaysPicked() {
        List<List<Character>> board = List.of(
                List.of('X', 'O', 'X'),
                List.of('O', 'X', 'O'),
                List.of('O', 'X', '-')
        );

        MoveGenerator.Cell move = moveGenerator.nextMove(board);

        assertThat(move).isEqualTo(new MoveGenerator.Cell(2, 2));
    }

    @Test
    void aFullBoardHasNoLegalMove() {
        List<List<Character>> fullBoard = List.of(
                List.of('X', 'O', 'X'),
                List.of('O', 'X', 'O'),
                List.of('O', 'X', 'O')
        );

        assertThatThrownBy(() -> moveGenerator.nextMove(fullBoard))
                .isInstanceOf(IllegalStateException.class);
    }
}
