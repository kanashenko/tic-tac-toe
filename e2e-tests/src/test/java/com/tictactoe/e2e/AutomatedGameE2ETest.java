package com.tictactoe.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
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

/**
 * Full-stack, black-box regression test for the automated Tic Tac Toe flow.
 *
 * <p>Spins up the entire stack (Eureka, Game Engine, Session Service, Gateway)
 * from {@code docker-compose.yaml} via Testcontainers, then drives it exactly
 * the way {@code index.html} does: create a session through the Gateway,
 * subscribe to its WebSocket feed, trigger the simulation, and watch the
 * pushed session snapshots until the game reaches a terminal status.
 *
 * <p>The test deliberately depends on nothing but the public HTTP/WebSocket
 * contract — see {@link SessionResponse}, {@link SessionStatus} and
 * {@link MoveDto} — so it exercises the services the same way any external
 * client would, without a compile-time dependency on session-service internals.
 */
@Testcontainers
class AutomatedGameE2ETest {

    private static final int GATEWAY_INTERNAL_PORT = 8080;

    @Container
    private static final ComposeContainer environment =
            new ComposeContainer(new File("docker-compose.yaml"))
                    // No Actuator on the classpath here — Spring Boot's own startup
                    // log line ("Started GatewayApplication in ...") is a reliable
                    // signal that the context (and its embedded Netty server) is up.
                    .withExposedService("gateway", GATEWAY_INTERNAL_PORT,
                            Wait.forLogMessage(".*Started GatewayApplication.*\\n", 1));

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

        // 1. Create Session.
        // The gateway becomes reachable before session-service has registered
        // itself with Eureka and been picked up by the gateway's load balancer
        // cache, so the first few requests routinely 503 — retry generously
        // rather than adding artificial startup delays.
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
                // The server closes the socket normally once the game reaches a
                // terminal status — that has to terminate our sink too, or the
                // StepVerifier below waits forever for a completion signal that
                // will never come.
                .doOnSuccess(_ -> inboundWsMessagesSink.tryEmitComplete())
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
                    .expectComplete()
                    // Bounded, rather than the unbounded default: a regression that
                    // stops the WS stream from completing should fail the test, not
                    // hang the build forever.
                    .verify(Duration.ofSeconds(60));
        } finally {
            if (!wsSubscription.isDisposed()) {
                wsSubscription.dispose();
            }
        }
    }
}