package com.tictactoe.gameengine.repository;

import com.tictactoe.gameengine.exception.GameAlreadyExistsException;
import com.tictactoe.gameengine.exception.GameNotFoundException;
import com.tictactoe.gameengine.domain.Game;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

/**
 * In-memory store of {@link Game} state, keyed by game ID — per the
 * assignment, game state only needs to live for the duration of the process.
 */
@Repository
public class GameRepository {

    private final ConcurrentHashMap<String, Game> games = new ConcurrentHashMap<>();

    /**
     * Stores a newly created game.
     *
     * @throws GameAlreadyExistsException if a game with this ID already exists
     */
    public Game save(Game game) {
        Game existing = games.putIfAbsent(game.gameId(), game);
        if (existing != null) {
            throw new GameAlreadyExistsException(game.gameId());
        }
        return game;
    }

    /** Looks up a game by ID, if it exists. */
    public Optional<Game> findById(String gameId) {
        return Optional.ofNullable(games.get(gameId));
    }

    /**
     * Applies {@code updater} to the stored game via {@link ConcurrentHashMap#compute},
     * so concurrent moves on the same game can't race on a read-modify-write.
     *
     * @throws GameNotFoundException if {@code gameId} does not exist
     */
    public Game update(String gameId, UnaryOperator<Game> updater) {
        return games.compute(gameId, (_, existing) -> {
            if (existing == null) {
                throw new GameNotFoundException(gameId);
            }
            return updater.apply(existing);
        });
    }
}
