package com.tictactoe.gameengine.domain;

import com.tictactoe.gameengine.exception.InvalidMoveException;

public record Game(
        String gameId,
        Board board,
        GameStatus status,
        Player nextTurn,
        Player winner
) {
    public static Game start(String gameId) {
        return new Game(gameId, Board.empty(), GameStatus.IN_PROGRESS, Player.X, null);
    }

    public Game applyMove(Player player, int row, int col) {
        if (status != GameStatus.IN_PROGRESS) {
            throw new InvalidMoveException("Game " + gameId + " has already ended");
        }
        if (player != nextTurn) {
            throw new InvalidMoveException("It is not " + player + "'s turn");
        }
        if (board.isOccupied(row, col)) {
            throw new InvalidMoveException("Cell (" + row + "," + col + ") is already occupied");
        }

        Board updatedBoard = board.place(row, col, player.getSymbol());
        var winningSymbol = updatedBoard.winningSymbol();

        if (winningSymbol.isPresent()) {
            return new Game(gameId, updatedBoard, GameStatus.WIN, null, player);
        }
        if (updatedBoard.isFull()) {
            return new Game(gameId, updatedBoard, GameStatus.DRAW, null, null);
        }
        return new Game(gameId, updatedBoard, GameStatus.IN_PROGRESS, player.opponent(), null);
    }
}