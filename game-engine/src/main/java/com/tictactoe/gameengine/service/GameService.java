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

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final GameRepository gameRepository;

    public Game createGame(String gameId) {
        Game game = gameRepository.save(Game.start(gameId));
        log.info("Created game {}", gameId);
        return game;
    }

    public Game makeMove(String gameId, MoveRequest request) {
        Player player = Player.valueOf(request.player());
        Game result = gameRepository.update(gameId, game -> game.applyMove(player, request.row(), request.col()));
        log.info("Game {} move by {} -> status {}", gameId, player, result.status());
        return result;
    }

    public Game getGame(String gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
    }
}
