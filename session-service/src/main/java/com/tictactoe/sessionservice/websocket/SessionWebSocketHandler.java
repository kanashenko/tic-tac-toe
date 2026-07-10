package com.tictactoe.sessionservice.websocket;

import com.tictactoe.sessionservice.service.SimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

/**
 * Handles {@code /ws/sessions?sessionId=...} connections: streams every
 * {@link com.tictactoe.sessionservice.dto.SessionResponse} published for a
 * session (see {@link SessionEventBroadcaster}) to the client as JSON text
 * frames, until the session's simulation ends or the client disconnects.
 *
 * <p>There is no reconnect/resume support by design — a dropped connection
 * simply stops the associated simulation (see {@link SimulationService#stop}),
 * matching the demo scope of this assignment rather than a production SLA.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionWebSocketHandler implements WebSocketHandler {

    private final SessionEventBroadcaster broadcaster;
    private final SimulationService simulationService;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String sessionId = extractSessionId(session);
        if (!StringUtils.hasText(sessionId)) {
            log.warn("Rejecting WebSocket connection: missing 'sessionId' query parameter");
            return session.close(CloseStatus.BAD_DATA.withReason("Missing sessionId query parameter"));
        }

        log.info("WebSocket connected for session {}", sessionId);

        Flux<WebSocketMessage> outbound = broadcaster.subscribe(sessionId)
                .map(objectMapper::writeValueAsString)
                .map(session::textMessage)
                .doFinally(signal -> {
                    log.info("WebSocket closed for session {} (signal: {})", sessionId, signal);
                    broadcaster.complete(sessionId);
                    simulationService.stop(sessionId);
                });

        return session.send(outbound);
    }

    private String extractSessionId(WebSocketSession session) {
        return UriComponentsBuilder.fromUri(session.getHandshakeInfo().getUri())
                .build()
                .getQueryParams()
                .getFirst("sessionId");
    }
}
