package com.tictactoe.gameengine.controller;

import com.tictactoe.gameengine.dto.GameResponse;
import com.tictactoe.gameengine.dto.MoveRequest;
import com.tictactoe.gameengine.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for the Game Engine service: create games, submit moves, and read
 * back the current board/status.
 */
@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    /** Creates a new game with an empty board, X to move first. */
    @PostMapping("/{gameId}")
    @ResponseStatus(HttpStatus.CREATED)
    public GameResponse createGame(@PathVariable String gameId) {
        return GameResponse.from(gameService.createGame(gameId));
    }

    /** Validates and applies a move, returning the resulting board and status. */
    @PostMapping("/{gameId}/move")
    public GameResponse move(@PathVariable String gameId, @Valid @RequestBody MoveRequest request) {
        return GameResponse.from(gameService.makeMove(gameId, request));
    }

    /** Returns the current board and status for an existing game. */
    @GetMapping("/{gameId}")
    public GameResponse getGame(@PathVariable String gameId) {
        return GameResponse.from(gameService.getGame(gameId));
    }
}
