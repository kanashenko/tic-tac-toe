package com.tictactoe.e2e;

/**
 * Mirrors {@code com.tictactoe.sessionservice.dto.MoveDto} — one entry of a
 * session's move history, as served over REST and pushed over the WebSocket.
 */
public record MoveDto(int moveNumber, String player, int row, int col) {
}
