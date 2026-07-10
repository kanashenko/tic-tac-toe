package com.tictactoe.sessionservice.client;

import com.tictactoe.sessionservice.client.dto.GameEngineMoveRequest;
import com.tictactoe.sessionservice.client.dto.GameEngineResponse;
import com.tictactoe.sessionservice.exception.GameEngineCommunicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Reactive client for the Game Engine's {@code /games} API, resolved via the
 * load-balanced {@link WebClient}. Non-2xx responses are converted into {@link GameEngineCommunicationException}
 */
@Component
@RequiredArgsConstructor
public class GameEngineClient {

    private final WebClient gameEngineWebClient;

    public Mono<GameEngineResponse> createGame(String gameId) {
        return gameEngineWebClient.post()
                .uri("/games/{gameId}", gameId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(GameEngineResponse.class);
    }

    public Mono<GameEngineResponse> makeMove(String gameId, GameEngineMoveRequest request) {
        return gameEngineWebClient.post()
                .uri("/games/{gameId}/move", gameId)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(GameEngineResponse.class);
    }

    /** Fetches the current state of a game, erroring if it does not exist. */
    public Mono<GameEngineResponse> getGame(String gameId) {
        return gameEngineWebClient.get()
                .uri("/games/{gameId}", gameId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(GameEngineResponse.class);
    }

    /**
     * Fetches a game's state, or emits empty if the Game Engine has no such
     * game yet. Games are created lazily — only when a session's simulation
     * starts (see {@link #ensureGame(String)}) — so a 404 here is the expected
     * "not started yet" state rather than a failure.
     */
    public Mono<GameEngineResponse> getGameIfExists(String gameId) {
        return getGame(gameId)
                .onErrorResume(GameEngineCommunicationException.class, ex ->
                        ex.getStatusCode() == HttpStatus.NOT_FOUND.value() ? Mono.empty() : Mono.error(ex));
    }

    /**
     * Returns a game's current state, creating it first if it does not exist.
     * This is where a session's game is lazily initialized: at the moment its
     * simulation starts rather than at session-creation time, which keeps
     * {@code POST /sessions} a single local write with no cross-service call to
     * keep consistent.
     */
    public Mono<GameEngineResponse> ensureGame(String gameId) {
        return getGameIfExists(gameId)
                .switchIfEmpty(createGame(gameId));
    }

    private Mono<? extends Throwable> mapError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("Game Engine returned " + response.statusCode())
                .map(body -> new GameEngineCommunicationException(response.statusCode().value(), body));
    }
}
