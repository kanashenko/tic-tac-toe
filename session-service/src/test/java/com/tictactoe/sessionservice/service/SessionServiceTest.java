package com.tictactoe.sessionservice.service;

import com.tictactoe.sessionservice.client.GameEngineClient;
import com.tictactoe.sessionservice.client.dto.GameEngineResponse;
import com.tictactoe.sessionservice.client.dto.GameEngineStatus;
import com.tictactoe.sessionservice.dto.SessionResponse;
import com.tictactoe.sessionservice.exception.SessionNotFoundException;
import com.tictactoe.sessionservice.mapper.SessionMapper;
import com.tictactoe.sessionservice.model.MoveEntity;
import com.tictactoe.sessionservice.model.SessionEntity;
import com.tictactoe.sessionservice.model.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Exercises {@link SessionService}'s orchestration logic with the Game Engine
 * client and persistence layer mocked out, so the reactive wiring (flatMap,
 * zip, lazy-game fallback) is verified independently of a real HTTP call or
 * database — those are covered instead by {@code AutomatedGameE2ETest}.
 */
@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionPersistenceService persistenceService;
    @Mock
    private GameEngineClient gameEngineClient;
    @Mock
    private SessionMapper sessionMapper;
    @Mock
    private SimulationService simulationService;

    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new SessionService(persistenceService, gameEngineClient, sessionMapper, simulationService);
    }

    @Test
    void createSessionPersistsLocallyWithoutTouchingTheGameEngine() {
        SessionEntity entity = SessionEntity.builder().id("s1").status(SessionStatus.IN_PROGRESS).build();
        SessionResponse expected = new SessionResponse("s1", SessionStatus.IN_PROGRESS, null, List.of(), List.of());

        when(sessionMapper.toNewEntity(anyString())).thenReturn(entity);
        when(persistenceService.createSession(entity)).thenReturn(entity);
        when(sessionMapper.toPendingResponse(entity, List.of())).thenReturn(expected);

        StepVerifier.create(sessionService.createSession())
                .expectNext(expected)
                .verifyComplete();

        // The game is created lazily at simulation time, not here.
        verifyNoInteractions(gameEngineClient);
    }

    @Test
    void getSessionMergesStoredMovesWithLiveGameStateWhenTheGameExists() {
        SessionEntity entity = SessionEntity.builder().id("s1").status(SessionStatus.IN_PROGRESS).build();
        List<MoveEntity> moves = List.of(MoveEntity.builder().sessionId("s1").moveNumber(0).build());
        GameEngineResponse liveState = new GameEngineResponse("s1", List.of(), GameEngineStatus.IN_PROGRESS, null);
        SessionResponse expected = new SessionResponse("s1", SessionStatus.IN_PROGRESS, null, List.of(), List.of());

        when(persistenceService.findSession("s1")).thenReturn(entity);
        when(persistenceService.findMoves("s1")).thenReturn(moves);
        when(gameEngineClient.getGameIfExists("s1")).thenReturn(Mono.just(liveState));
        when(sessionMapper.toSessionResponse(entity, moves, liveState)).thenReturn(expected);

        StepVerifier.create(sessionService.getSession("s1"))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void getSessionReturnsPendingStateWhenTheGameHasNotStartedYet() {
        SessionEntity entity = SessionEntity.builder().id("s1").status(SessionStatus.IN_PROGRESS).build();
        List<MoveEntity> moves = List.of();
        SessionResponse pending = new SessionResponse("s1", SessionStatus.IN_PROGRESS, null, List.of(), List.of());

        when(persistenceService.findSession("s1")).thenReturn(entity);
        when(persistenceService.findMoves("s1")).thenReturn(moves);
        when(gameEngineClient.getGameIfExists("s1")).thenReturn(Mono.empty());
        when(sessionMapper.toPendingResponse(entity, moves)).thenReturn(pending);

        StepVerifier.create(sessionService.getSession("s1"))
                .expectNext(pending)
                .verifyComplete();
    }

    @Test
    void getSessionPropagatesNotFoundForAnUnknownSession() {
        when(persistenceService.findSession("missing")).thenThrow(new SessionNotFoundException("missing"));

        StepVerifier.create(sessionService.getSession("missing"))
                .expectError(SessionNotFoundException.class)
                .verify();
    }

    @Test
    void startSimulationTriggersSimulationWhenTheSessionExists() {
        SessionEntity entity = SessionEntity.builder().id("s1").status(SessionStatus.IN_PROGRESS).build();
        when(persistenceService.findSession("s1")).thenReturn(entity);

        StepVerifier.create(sessionService.startSimulation("s1"))
                .verifyComplete();

        verify(simulationService).simulate("s1");
    }

    @Test
    void startSimulationRejectsUnknownSessionWithoutSimulating() {
        when(persistenceService.findSession("missing")).thenThrow(new SessionNotFoundException("missing"));

        StepVerifier.create(sessionService.startSimulation("missing"))
                .expectError(SessionNotFoundException.class)
                .verify();

        // A bogus session must never kick off a game in the Game Engine.
        verifyNoInteractions(simulationService);
    }
}
