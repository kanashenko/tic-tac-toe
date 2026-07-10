package com.tictactoe.sessionservice.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Picks the next move for whichever player is up, per the assignment's
 * "simple random... algorithm" option — collects every empty cell and
 * chooses one uniformly at random.
 */
@Component
public class MoveGenerator {

    private final Random random = new Random();

    /**
     * Returns a randomly chosen empty cell on the given board.
     *
     * @throws IllegalStateException if the board has no empty cells; callers
     *                                are expected to stop playing once the
     *                                Game Engine reports the game has ended
     */
    public Cell nextMove(List<List<Character>> board) {
        List<Cell> emptyCells = new ArrayList<>();
        for (int r = 0; r < board.size(); r++) {
            List<Character> rowCells = board.get(r);
            for (int c = 0; c < rowCells.size(); c++) {
                if (rowCells.get(c) == '-') {
                    emptyCells.add(new Cell(r, c));
                }
            }
        }
        if (emptyCells.isEmpty()) {
            throw new IllegalStateException("No empty cells available for move generation");
        }
        return emptyCells.get(random.nextInt(emptyCells.size()));
    }

    /** A zero-based board position. */
    public record Cell(int row, int col) {
    }
}
