package com.tictactoe.gameengine.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** The two Tic Tac Toe players and the board symbol each one plays. */
@Getter
@RequiredArgsConstructor
public enum Player {
    X('X'),
    O('O');

    private final char symbol;

    public Player opponent() {
        return this == X ? O : X;
    }
}
