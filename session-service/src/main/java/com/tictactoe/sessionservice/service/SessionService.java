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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionPersistenceService persistenceService;
    private final GameEngineClient gameEngineClient;
    private final SessionMapper sessionMapper;

    public Mono<SessionResponse> createSession() {
        String sessionId = UUID.randomUUID().toString();
        return gameEngineClient.createGame(sessionId)
                .flatMap(engineResponse -> saveNewSession(sessionId, engineResponse)
                        .map(entity -> sessionMapper.toSessionResponse(entity, List.of(), engineResponse)))
                .doOnSuccess(response -> log.info("Created session {}", sessionId));
    }

    public Mono<SessionResponse> getSession(String sessionId) {
        return findSession(sessionId)
                .flatMap(entity -> Mono.zip(findMoves(sessionId), gameEngineClient.getGame(sessionId))
                        .map(tuple -> sessionMapper.toSessionResponse(entity, tuple.getT1(), tuple.getT2())));
    }

    private Mono<SessionEntity> saveNewSession(String sessionId, GameEngineResponse engineResponse) {
        SessionEntity entity = sessionMapper.toNewEntity(sessionId, engineResponse);
        return Mono.fromCallable(() -> persistenceService.createSession(entity))
                .subscribeOn(Schedulers.boundedElastic());
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