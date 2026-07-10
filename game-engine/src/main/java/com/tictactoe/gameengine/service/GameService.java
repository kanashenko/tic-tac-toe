package com.tictactoe.gameengine.service;

import com.tictactoe.gameengine.exception.GameAlreadyExistsException;
import com.tictactoe.gameengine.exception.GameNotFoundException;
import com.tictactoe.gameengine.domain.Game;
import com.tictactoe.gameengine.dto.MoveRequest;
import com.tictactoe.gameengine.domain.Player;
import com.tictactoe.gameengine.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Application service backing {@link com.tictactoe.gameengine.controller.GameController}.
 * Translates DTOs to/from the {@link Game} domain model and delegates state
 * management to {@link GameRepository}; move validation itself lives in
 * {@link Game#applyMove(Player, int, int)}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final GameRepository gameRepository;

    /**
     * Starts a new game on an empty board.
     *
     * @throws GameAlreadyExistsException if {@code gameId} is already in use
     */
    public Game createGame(String gameId) {
        Game game = gameRepository.save(Game.start(gameId));
        log.info("Created game {}", gameId);
        return game;
    }

    /**
     * Applies the move described by {@code request} to the given game.
     *
     * @throws GameNotFoundException                                if {@code gameId} does not exist
     * @throws com.tictactoe.gameengine.exception.InvalidMoveException if the move is illegal
     */
    public Game makeMove(String gameId, MoveRequest request) {
        Player player = Player.valueOf(request.player());
        Game result = gameRepository.update(gameId, game -> game.applyMove(player, request.row(), request.col()));
        log.info("Game {} move by {} -> status {}", gameId, player, result.status());
        return result;
    }

    /**
     * Returns the current state of a game.
     *
     * @throws GameNotFoundException if {@code gameId} does not exist
     */
    public Game getGame(String gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
    }
}
