package com.tictactoe.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.Disposable;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class AutomatedGameFlowE2ETest {

    private static final int GATEWAY_INTERNAL_PORT = 8080;

    @Container
    private static final ComposeContainer environment =
            new ComposeContainer(new File("src/test/resources/docker-compose-test.yml"))
                    .withExposedService("gateway", GATEWAY_INTERNAL_PORT,
                            Wait.forHttp("/actuator/health").forStatusCode(200));

    @Test
    void testAutomatedGameFlow() {
        String host = environment.getServiceHost("gateway", GATEWAY_INTERNAL_PORT);
        int port = environment.getServicePort("gateway", GATEWAY_INTERNAL_PORT);
        String baseUrl = "http://" + host + ":" + port;

        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
        WebSocketClient wsClient = new ReactorNettyWebSocketClient();
        ObjectMapper objectMapper = new ObjectMapper();

        Sinks.Many<SessionResponse> inboundWsMessagesSink = Sinks.many().replay().all();
        Sinks.One<Void> wsConnectedSink = Sinks.one();

        // 1. Create Session
        SessionResponse session = webClient.post()
                .uri("/sessions")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(SessionResponse.class)
                .retryWhen(Retry.fixedDelay(20, Duration.ofSeconds(5)))
                .block(Duration.ofSeconds(120));

        assertThat(session).isNotNull();
        String sessionId = session.sessionId();
        URI wsUri = URI.create("ws://" + host + ":" + port + "/ws/sessions?sessionId=" + sessionId);

        // 2. Open WebSocket and Synchronize
        Disposable wsSubscription = wsClient.execute(wsUri, wsSession -> {
                    wsConnectedSink.tryEmitEmpty(); // Signal that the channel is open
                    return wsSession.receive()
                            .map(WebSocketMessage::getPayloadAsText)
                            .map(payload -> {
                                try {
                                    return objectMapper.readValue(payload, SessionResponse.class);
                                } catch (Exception e) {
                                    throw new RuntimeException("Failed to parse WS payload", e);
                                }
                            })
                            .doOnNext(inboundWsMessagesSink::tryEmitNext)
                            .then();
                })
                .retryWhen(Retry.fixedDelay(15, Duration.ofSeconds(3)))
                .doOnError(err -> inboundWsMessagesSink.tryEmitError(
                        new IllegalStateException("WebSocket connection failed", err)))
                .subscribe();

        // 3. Trigger Simulation ONLY after WS is confirmed connected
        wsConnectedSink.asMono()
                .then(webClient.post()
                        .uri("/sessions/{id}/simulate", sessionId)
                        .retrieve()
                        .toBodilessEntity()
                        .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))
                )
                .block(Duration.ofSeconds(15));

        // 4. Verify the Game Stream
        try {
            StepVerifier.create(inboundWsMessagesSink.asFlux())
                    .thenConsumeWhile(response -> {
                        System.out.println("Frame Received Status: " + response.status());
                        return response.status() == SessionStatus.IN_PROGRESS;
                    })
                    .assertNext(finalResponse -> {
                        System.out.println("Terminal state reached: " + finalResponse.status());
                        assertThat(finalResponse.status())
                                .isIn(SessionStatus.WIN, SessionStatus.DRAW, SessionStatus.ERROR);
                    })
                    .verifyComplete();
        } finally {
            if (wsSubscription != null && !wsSubscription.isDisposed()) {
                wsSubscription.dispose();
            }
        }
    }
}