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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plays out a session automatically: alternates X/O moves, forwards each one
 * to the Game Engine, persists the result, and broadcasts it to any
 * WebSocket subscriber via {@link SessionEventBroadcaster}.
 *
 * <p>Each session's move loop is a subscribed {@link Mono} tracked in
 * {@link #activeSimulations}, keyed by session ID. This both prevents two
 * concurrent {@link #simulate(String)} calls for the same session from
 * running two loops, and lets {@link #stop(String)} cancel a loop once
 * nobody is listening for its output anymore.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SimulationService {

    @Value("${simulation.move-delay}")
    private final Duration moveDelay;

    private final GameEngineClient gameEngineClient;
    private final SessionPersistenceService persistenceService;
    private final MoveGenerator moveGenerator;
    private final SessionEventBroadcaster broadcaster;
    private final SessionMapper sessionMapper;

    private final Map<String, Disposable> activeSimulations = new ConcurrentHashMap<>();

    /** The Game Engine's state after the previous move, and how many moves have been played so far. */
    private record SimulationState(GameEngineResponse game, int moveNumber) {
    }

    /**
     * Starts the move loop for a session, unless one is already running for it.
     * Returns immediately — the loop itself runs asynchronously and reports
     * progress through {@link SessionEventBroadcaster}.
     */
    public void simulate(String sessionId) {
        if (activeSimulations.containsKey(sessionId)) {
            log.warn("Simulation already running for session {}, ignoring duplicate trigger", sessionId);
            return;
        }
        log.info("Starting simulation for session {}", sessionId);
        // computeIfAbsent guarantees a single subscription even under a concurrent
        // trigger. The subscribed chain's doFinally removes this same key, which
        // ConcurrentHashMap forbids *during* computation — this is safe only
        // because buildSimulation starts with an async WebClient call, so
        // subscribe() always returns before the chain can terminate. Keep the
        // first operation async; a synchronous source here would let doFinally
        // run mid-computation and break the map.
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

    /**
     * Builds the reactive move loop for one session: lazily create the game if
     * it doesn't exist yet (this is where a session's game is initialized in
     * the Game Engine), then repeatedly play a move and re-check the status via
     * {@link Mono#expand}, until the Game Engine reports anything other than
     * {@link GameEngineStatus#IN_PROGRESS}.
     */
    private Mono<Void> buildSimulation(String sessionId) {
        return gameEngineClient.ensureGame(sessionId)
                .doOnNext(initial -> log.debug("Game ready for session {}: {}", sessionId, initial.status()))
                .map(initial -> new SimulationState(initial, 0))
                .expand(this::playNextMove)
                .then()
                .doFinally(signal -> {
                    log.info("Simulation finished for session {} (signal: {})", sessionId, signal);
                    activeSimulations.remove(sessionId);
                })
                .onErrorResume(ex -> reportError(sessionId, null, ex));
    }

    /**
     * Plays one move if the game is still in progress: waits {@link #moveDelay}
     * (so the UI can visibly animate each move), asks {@link MoveGenerator}
     * for a legal cell, submits it to the Game Engine, then persists and
     * broadcasts the result. Returns {@link Mono#empty()} once the game has
     * ended, which is what stops {@link Mono#expand} from recursing further.
     */
    private Mono<SimulationState> playNextMove(SimulationState state) {
        if (state.game().status() != GameEngineStatus.IN_PROGRESS) {
            log.debug("Game {} no longer in progress ({}), stopping move loop", state.game().gameId(), state.game().status());
            return Mono.empty();
        }

        String player = state.moveNumber() % 2 == 0 ? "X" : "O";
        MoveGenerator.Cell cell = moveGenerator.nextMove(state.game().board());
        GameEngineMoveRequest request = new GameEngineMoveRequest(player, cell.row(), cell.col());

        log.info("Session {} move #{}: {} plays [{}, {}]",
                state.game().gameId(), state.moveNumber(), player, cell.row(), cell.col());

        return Mono.delay(moveDelay)
                .flatMap(_ -> gameEngineClient.makeMove(state.game().gameId(), request))
                .flatMap(updated -> persistAndBroadcast(state.game().gameId(), state.moveNumber(), player, cell, updated)
                        .thenReturn(new SimulationState(updated, state.moveNumber() + 1)))
                .onErrorResume(ex -> reportError(state.game().gameId(), state.game(), ex)
                        .then(Mono.empty()));
    }

    /**
     * Reports a simulation failure: pushes an error event to the WebSocket
     * (both non-blocking sink operations) and marks the session errored in the
     * database. The DB write is the blocking part, so it's offloaded to
     * {@link Schedulers#boundedElastic()} — this method is invoked from error
     * callbacks that may run on a Netty event-loop thread, which must never
     * block. Returns a {@link Mono} to fold cleanly into the calling chain.
     */
    private Mono<Void> reportError(String sessionId, GameEngineResponse lastKnown, Throwable ex) {
        log.error("Simulation failed for session {}", sessionId, ex);
        broadcaster.publish(sessionId, sessionMapper.toErrorEvent(sessionId, lastKnown));
        broadcaster.complete(sessionId);
        return Mono.fromRunnable(() -> persistenceService.markSessionError(sessionId))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private Mono<Void> persistAndBroadcast(String sessionId, int moveNumber, String player,
                                           MoveGenerator.Cell cell, GameEngineResponse updated) {
        MoveEntity move = sessionMapper.toEntity(sessionId, moveNumber, player, cell);
        SessionStatus status = sessionMapper.toSessionStatus(updated.status());

        return Mono.fromRunnable(() -> persistenceService.recordMove(move, status, updated.winner()))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(_ -> {
                    broadcaster.publish(sessionId, sessionMapper.toEvent(sessionId, move, updated));
                    if (updated.status() != GameEngineStatus.IN_PROGRESS) {
                        broadcaster.complete(sessionId);
                    }
                })
                .then();
    }
}
