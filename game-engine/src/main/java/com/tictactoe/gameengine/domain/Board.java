package com.tictactoe.gameengine.domain;

import java.util.*;

public record Board(List<Character> cells) {

    public static final char EMPTY = '-';
    private static final int SIZE = 3;

    private static final List<int[]> WIN_LINES = List.of(
            new int[]{0, 1, 2}, new int[]{3, 4, 5}, new int[]{6, 7, 8},
            new int[]{0, 3, 6}, new int[]{1, 4, 7}, new int[]{2, 5, 8},
            new int[]{0, 4, 8}, new int[]{2, 4, 6}
    );

    public Board {
        cells = List.copyOf(cells);
    }

    public static Board empty() {
        return new Board(Collections.nCopies(SIZE * SIZE, EMPTY));
    }

    public char at(int row, int col) {
        return cells.get(index(row, col));
    }

    public boolean isOccupied(int row, int col) {
        return at(row, col) != EMPTY;
    }

    public boolean isFull() {
        return cells.stream().noneMatch(c -> c == EMPTY);
    }

    public Board place(int row, int col, char symbol) {
        List<Character> updated = new ArrayList<>(cells);
        updated.set(index(row, col), symbol);
        return new Board(List.copyOf(updated));
    }

    public Optional<Character> winningSymbol() {
        return WIN_LINES.stream()
                .map(this::lineSymbols)
                .filter(this::isWinningLine)
                .map(List::getFirst)
                .findFirst();
    }

    public List<List<Character>> asRows() {
        List<List<Character>> rows = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            rows.add(cells.subList(r * SIZE, r * SIZE + SIZE));
        }
        return List.copyOf(rows);
    }

    private List<Character> lineSymbols(int[] line) {
        return Arrays.stream(line).mapToObj(cells::get).toList();
    }

    private boolean isWinningLine(List<Character> line) {
        return line.getFirst() != EMPTY && line.stream().distinct().count() == 1;
    }

    private static int index(int row, int col) {
        return row * SIZE + col;
    }
}
