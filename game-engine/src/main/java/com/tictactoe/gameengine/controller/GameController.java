package com.tictactoe.gameengine.controller;

import com.tictactoe.gameengine.dto.GameResponse;
import com.tictactoe.gameengine.dto.MoveRequest;
import com.tictactoe.gameengine.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/{gameId}")
    @ResponseStatus(HttpStatus.CREATED)
    public GameResponse createGame(@PathVariable String gameId) {
        return GameResponse.from(gameService.createGame(gameId));
    }

    @PostMapping("/{gameId}/move")
    public GameResponse move(@PathVariable String gameId, @Valid @RequestBody MoveRequest request) {
        return GameResponse.from(gameService.makeMove(gameId, request));
    }

    @GetMapping("/{gameId}")
    public GameResponse getGame(@PathVariable String gameId) {
        return GameResponse.from(gameService.getGame(gameId));
    }
}