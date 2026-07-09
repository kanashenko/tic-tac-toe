package com.tictactoe.gameengine.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MoveRequest(
        @NotBlank @Pattern(regexp = "X|O") String player,
        @Min(0) @Max(2) int row,
        @Min(0) @Max(2) int col
) {}
