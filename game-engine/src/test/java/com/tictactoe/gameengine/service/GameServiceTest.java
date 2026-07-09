package com.tictactoe.gameengine.service;

import com.tictactoe.gameengine.domain.Game;
import com.tictactoe.gameengine.domain.GameStatus;
import com.tictactoe.gameengine.dto.MoveRequest;
import com.tictactoe.gameengine.exception.GameNotFoundException;
import com.tictactoe.gameengine.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameService(gameRepository);
    }

    @Test
    void createGameDelegatesAFreshGameToTheRepository() {
        when(gameRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Game created = gameService.createGame("g1");

        assertThat(created.gameId()).isEqualTo("g1");
        assertThat(created.status()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    void makeMoveAppliesTheRequestedMoveThroughTheRepositoryUpdater() {
        Game current = Game.start("g1");
        when(gameRepository.update(eq("g1"), any())).thenAnswer(invocation -> {
            UnaryOperator<Game> updater = invocation.getArgument(1);
            return updater.apply(current);
        });

        Game result = gameService.makeMove("g1", new MoveRequest("X", 0, 0));

        assertThat(result.board().at(0, 0)).isEqualTo('X');
        assertThat(result.nextTurn().name()).isEqualTo("O");
    }

    @Test
    void getGameReturnsTheStoredGameWhenPresent() {
        Game existing = Game.start("g1");
        when(gameRepository.findById("g1")).thenReturn(Optional.of(existing));

        assertThat(gameService.getGame("g1")).isEqualTo(existing);
    }

    @Test
    void getGameThrowsWhenTheGameIsUnknown() {
        when(gameRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.getGame("missing"))
                .isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void makeMoveLetsRepositoryExceptionsPropagate() {
        when(gameRepository.update(eq("missing"), any()))
                .thenThrow(new GameNotFoundException("missing"));

        assertThatThrownBy(() -> gameService.makeMove("missing", new MoveRequest("X", 0, 0)))
                .isInstanceOf(GameNotFoundException.class);
        verify(gameRepository).update(eq("missing"), any());
    }
}
