package com.tictactoe.gameengine.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BoardTest {

    @Test
    void emptyBoardHasAllCellsBlank() {
        Board board = Board.empty();

        assertThat(board.cells()).hasSize(9).containsOnly(Board.EMPTY);
        assertThat(board.isFull()).isFalse();
        assertThat(board.winningSymbol()).isEmpty();
    }

    @Test
    void placeReturnsANewBoardAndLeavesTheOriginalUntouched() {
        Board original = Board.empty();

        Board updated = original.place(0, 0, 'X');

        assertThat(original.isOccupied(0, 0)).isFalse();
        assertThat(updated.isOccupied(0, 0)).isTrue();
        assertThat(updated.at(0, 0)).isEqualTo('X');
    }

    @Test
    void asRowsSlicesTheFlatCellListIntoA3x3Grid() {
        Board board = Board.empty().place(0, 0, 'X').place(1, 1, 'O').place(2, 2, 'X');

        List<List<Character>> rows = board.asRows();

        assertThat(rows).hasSize(3);
        assertThat(rows.getFirst().getFirst()).isEqualTo('X');
        assertThat(rows.get(1).get(1)).isEqualTo('O');
        assertThat(rows.get(2).get(2)).isEqualTo('X');
    }

    @ParameterizedTest
    @MethodSource("winningLines")
    void winningSymbolDetectsEveryLineOfThree(int[] line) {
        Board board = Board.empty();
        for (int index : line) {
            board = board.place(index / 3, index % 3, 'X');
        }

        assertThat(board.winningSymbol()).contains('X');
    }

    static Stream<int[]> winningLines() {
        return Stream.of(
                new int[]{0, 1, 2}, new int[]{3, 4, 5}, new int[]{6, 7, 8}, // rows
                new int[]{0, 3, 6}, new int[]{1, 4, 7}, new int[]{2, 5, 8}, // columns
                new int[]{0, 4, 8}, new int[]{2, 4, 6}                     // diagonals
        );
    }

    @Test
    void aFullBoardWithNoThreeInARowIsNotAWin() {
        // X | O | X
        // X | O | O
        // O | X | X
        Board board = Board.empty()
                .place(0, 0, 'X').place(0, 1, 'O').place(0, 2, 'X')
                .place(1, 0, 'X').place(1, 1, 'O').place(1, 2, 'O')
                .place(2, 0, 'O').place(2, 1, 'X').place(2, 2, 'X');

        assertThat(board.isFull()).isTrue();
        assertThat(board.winningSymbol()).isEmpty();
    }
}
