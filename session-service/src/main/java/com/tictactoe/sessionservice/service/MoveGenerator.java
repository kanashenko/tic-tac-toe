package com.tictactoe.sessionservice.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class MoveGenerator {

    private final Random random = new Random();

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

    public record Cell(int row, int col) {
    }
}