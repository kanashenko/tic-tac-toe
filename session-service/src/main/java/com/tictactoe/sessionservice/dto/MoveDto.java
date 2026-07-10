package com.tictactoe.sessionservice.dto;

/**
 * One entry in a session's move history.
 *
 * @param moveNumber zero-based order in which the move was played
 * @param player     "X" or "O"
 * @param row        zero-based row, 0-2
 * @param col        zero-based column, 0-2
 */
public record MoveDto(int moveNumber, String player, int row, int col) {

}
