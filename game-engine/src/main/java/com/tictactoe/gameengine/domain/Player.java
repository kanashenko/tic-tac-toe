package com.tictactoe.gameengine.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
