package com.tictactoe.gameengine.domain;

import com.tictactoe.gameengine.exception.InvalidMoveException;

/**
 * A single Tic Tac Toe match: the board, whose turn it is, and how it ended
 * (if it has). Immutable — {@link #applyMove(Player, int, int)} returns a new
 * {@code Game} rather than mutating this one, so a reference to a past game
 * state is always safe to keep around (e.g. for logging or move history).
 *
 * @param gameId   the identifier this game is stored under in {@link com.tictactoe.gameengine.repository.GameRepository}
 * @param board    the current board state
 * @param status   whether the game is still being played, won, or drawn
 * @param nextTurn the player to move next, or {@code null} once the game has ended
 * @param winner   the winning player, or {@code null} if there is no winner (yet, or ever, in a draw)
 */
public record Game(
        String gameId,
        Board board,
        GameStatus status,
        Player nextTurn,
        Player winner
) {
    /** Starts a new game on an empty board, with X to move first as per standard Tic Tac Toe rules. */
    public static Game start(String gameId) {
        return new Game(gameId, Board.empty(), GameStatus.IN_PROGRESS, Player.X, null);
    }

    /**
     * Validates and applies a move, returning the resulting {@code Game}.
     *
     * @throws InvalidMoveException if the game has already ended, it isn't {@code player}'s
     *                               turn, or the target cell is already occupied
     */
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
