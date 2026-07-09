package com.tictactoe.sessionservice.service;

import com.tictactoe.sessionservice.client.GameEngineClient;
import com.tictactoe.sessionservice.client.dto.GameEngineMoveRequest;
import com.tictactoe.sessionservice.client.dto.GameEngineResponse;
import com.tictactoe.sessionservice.client.dto.GameEngineStatus;
import com.tictactoe.sessionservice.mapper.SessionMapper;
import com.tictactoe.sessionservice.model.MoveEntity;
import com.tictactoe.sessionservice.model.SessionStatus;
import com.tictactoe.sessionservice.websocket.SessionEventBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimulationService {

    @Value("${simulation.move-delay}")
    private Duration moveDelay;

    private final GameEngineClient gameEngineClient;
    private final SessionPersistenceService persistenceService;
    private final MoveGenerator moveGenerator;
    private final SessionEventBroadcaster broadcaster;
    private final SessionMapper sessionMapper;

    private final Map<String, Disposable> activeSimulations = new ConcurrentHashMap<>();

    private record SimulationState(GameEngineResponse game, int moveNumber) {
    }

    public void simulate(String sessionId) {
        if (activeSimulations.containsKey(sessionId)) {
            log.warn("Simulation already running for session {}, ignoring duplicate trigger", sessionId);
            return;
        }
        log.info("--- Simulation TRIGGERED for session: {}", sessionId);
        activeSimulations.computeIfAbsent(sessionId, id -> buildSimulation(id).subscribe());
    }

    /** Called when the watching WebSocket connection drops — stops a loop nobody is listening to. */
    public void stop(String sessionId) {
        Disposable disposable = activeSimulations.remove(sessionId);
        if (disposable != null) {
            log.info("Stopping simulation for session {} — no listener left", sessionId);
            disposable.dispose();
        }
    }

    private Mono<Void> buildSimulation(String sessionId) {
        return gameEngineClient.getGame(sessionId)
                .doOnNext(initial -> log.info("Fetched initial state for session {}. Status: {}", sessionId, initial.status()))
                .map(initial -> new SimulationState(initial, 0))
                .expand(this::playNextMove)
                .then()
                .doFinally(signal -> {
                    log.info("=== Simulation LOOP ENDED for session: {}. Signal: {}", sessionId, signal);
                    activeSimulations.remove(sessionId);
                })
                .doOnError(ex -> reportError(sessionId, null, ex))
                .onErrorResume(ex -> Mono.empty());
    }

    private Mono<SimulationState> playNextMove(SimulationState state) {
        if (state.game().status() != GameEngineStatus.IN_PROGRESS) {
            log.info("Game {} is not IN_PROGRESS (Status: {}). Halting moves.", state.game().gameId(), state.game().status());
            return Mono.empty();
        }

        String player = state.moveNumber() % 2 == 0 ? "X" : "O";
        MoveGenerator.Cell cell = moveGenerator.nextMove(state.game().board());
        GameEngineMoveRequest request = new GameEngineMoveRequest(player, cell.row(), cell.col());

        log.info("Move #{} -> Player {} playing at [{}, {}] for game {} (Delay: {})",
                state.moveNumber(), player, cell.row(), cell.col(), state.game().gameId(), moveDelay);

        return Mono.delay(moveDelay)
                .flatMap(tick -> gameEngineClient.makeMove(state.game().gameId(), request))
                .flatMap(updated -> persistAndBroadcast(state.game().gameId(), state.moveNumber(), player, cell, updated)
                        .thenReturn(new SimulationState(updated, state.moveNumber() + 1)))
                .onErrorResume(ex -> {
                    reportError(state.game().gameId(), state.game(), ex);
                    return Mono.empty();
                });
    }

    private void reportError(String sessionId, GameEngineResponse lastKnown, Throwable ex) {
        log.error("Simulation failed for session {}", sessionId, ex);
        broadcaster.publish(sessionId, sessionMapper.toErrorEvent(sessionId, lastKnown));
        broadcaster.complete(sessionId);
        persistenceService.markSessionError(sessionId);
    }

    private Mono<Void> persistAndBroadcast(String sessionId, int moveNumber, String player,
                                           MoveGenerator.Cell cell, GameEngineResponse updated) {
        MoveEntity move = sessionMapper.toEntity(sessionId, moveNumber, player, cell);
        SessionStatus status = sessionMapper.toSessionStatus(updated.status());

        return Mono.fromRunnable(() -> persistenceService.recordMove(move, status, updated.winner()))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(v -> {
                    broadcaster.publish(sessionId, sessionMapper.toEvent(sessionId, move, updated));
                    if (updated.status() != GameEngineStatus.IN_PROGRESS) {
                        broadcaster.complete(sessionId);
                    }
                })
                .then();
    }
}