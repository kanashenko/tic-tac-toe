package com.tictactoe.sessionservice.client.dto;

/** Request body for {@code POST /games/{gameId}/move} on the Game Engine. */
public record GameEngineMoveRequest(String player, int row, int col) {
}
