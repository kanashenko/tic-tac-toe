package com.tictactoe.gameengine.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * A move submitted to {@code POST /games/{gameId}/move}.
 *
 * @param player "X" or "O"
 * @param row    zero-based row, 0-2
 * @param col    zero-based column, 0-2
 */
public record MoveRequest(
        @NotBlank @Pattern(regexp = "[XO]") String player,
        @Min(0) @Max(2) int row,
        @Min(0) @Max(2) int col
) {}
