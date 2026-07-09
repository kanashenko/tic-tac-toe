package com.tictactoe.sessionservice.client.dto;

public record GameEngineMoveRequest(String player, int row, int col) {
}