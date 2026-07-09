package com.tictactoe.sessionservice.service;

import com.tictactoe.sessionservice.client.GameEngineClient;
import com.tictactoe.sessionservice.client.dto.GameEngineResponse;
import com.tictactoe.sessionservice.dto.SessionResponse;
import com.tictactoe.sessionservice.mapper.SessionMapper;
import com.tictactoe.sessionservice.model.MoveEntity;
import com.tictactoe.sessionservice.model.SessionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Session lifecycle operations: creating a session and reading back its current
 * state plus move history. Automated play-out itself is handled by
 * {@link SimulationService}.
 *
 * <p>The game in the Game Engine is created lazily — only when a simulation
 * starts (see {@link GameEngineClient#ensureGame(String)}) — so creating a
 * session is a single local write with nothing to keep consistent across
 * services. A session that exists but hasn't been simulated yet simply has no
 * game in the Game Engine, which {@link #getSession(String)} reports as an
 * empty board.
 *
 * <p>{@link SessionPersistenceService} wraps JPA's blocking repositories, so
 * every call into it here is shifted onto {@link Schedulers#boundedElastic()}
 * to keep the WebFlux event loop free.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionPersistenceService persistenceService;
    private final GameEngineClient gameEngineClient;
    private final SessionMapper sessionMapper;

    /**
     * Creates and persists a new session with a freshly generated ID. The
     * backing game is not initialized here; that happens the first time the
     * session is simulated.
     */
    public Mono<SessionResponse> createSession() {
        String sessionId = UUID.randomUUID().toString();
        SessionEntity entity = sessionMapper.toNewEntity(sessionId);
        return Mono.fromCallable(() -> persistenceService.createSession(entity))
                .subscribeOn(Schedulers.boundedElastic())
                .map(saved -> sessionMapper.toPendingResponse(saved, List.of()))
                .doOnSuccess(_ -> log.info("Created session {}", sessionId));
    }

    /**
     * Returns a session's stored move history alongside its game state. If the
     * game has already been initialized (its simulation has started), the live
     * board/status is fetched fresh from the Game Engine; otherwise the session
     * is reported from its local record with an empty board.
     *
     * @throws com.tictactoe.sessionservice.exception.SessionNotFoundException if {@code sessionId} is unknown
     */
    public Mono<SessionResponse> getSession(String sessionId) {
        return findSession(sessionId)
                .flatMap(entity -> Mono.zip(findMoves(sessionId), liveStateIfAny(sessionId))
                        .map(tuple -> tuple.getT2()
                                .map(live -> sessionMapper.toSessionResponse(entity, tuple.getT1(), live))
                                .orElseGet(() -> sessionMapper.toPendingResponse(entity, tuple.getT1()))));
    }

    private Mono<Optional<GameEngineResponse>> liveStateIfAny(String sessionId) {
        return gameEngineClient.getGameIfExists(sessionId)
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty());
    }

    private Mono<SessionEntity> findSession(String sessionId) {
        return Mono.fromCallable(() -> persistenceService.findSession(sessionId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<List<MoveEntity>> findMoves(String sessionId) {
        return Mono.fromCallable(() -> persistenceService.findMoves(sessionId))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
