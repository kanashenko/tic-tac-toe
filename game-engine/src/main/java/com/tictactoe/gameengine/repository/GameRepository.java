package com.tictactoe.gameengine.repository;

import com.tictactoe.gameengine.exception.GameAlreadyExistsException;
import com.tictactoe.gameengine.exception.GameNotFoundException;
import com.tictactoe.gameengine.domain.Game;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

@Repository
public class GameRepository {

    private final ConcurrentHashMap<String, Game> games = new ConcurrentHashMap<>();

    public Game save(Game game) {
        Game existing = games.putIfAbsent(game.gameId(), game);
        if (existing != null) {
            throw new GameAlreadyExistsException(game.gameId());
        }
        return game;
    }

    public Optional<Game> findById(String gameId) {
        return Optional.ofNullable(games.get(gameId));
    }

    public Game update(String gameId, UnaryOperator<Game> updater) {
        return games.compute(gameId, (id, existing) -> {
            if (existing == null) {
                throw new GameNotFoundException(gameId);
            }
            return updater.apply(existing);
        });
    }
}
