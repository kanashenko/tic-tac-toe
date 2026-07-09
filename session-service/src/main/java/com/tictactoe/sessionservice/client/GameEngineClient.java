package com.tictactoe.sessionservice.client;

import com.tictactoe.sessionservice.client.dto.GameEngineMoveRequest;
import com.tictactoe.sessionservice.client.dto.GameEngineResponse;
import com.tictactoe.sessionservice.exception.GameEngineCommunicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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

    public Mono<GameEngineResponse> getGame(String gameId) {
        return gameEngineWebClient.get()
                .uri("/games/{gameId}", gameId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(GameEngineResponse.class);
    }

    private Mono<? extends Throwable> mapError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("Game Engine returned " + response.statusCode())
                .map(body -> new GameEngineCommunicationException(response.statusCode().value(), body));
    }
}