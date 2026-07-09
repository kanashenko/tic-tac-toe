package com.tictactoe.sessionservice.websocket;

import com.tictactoe.sessionservice.service.SimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
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
            log.warn("WS connection rejected: Missing sessionId");
            return session.close(CloseStatus.BAD_DATA.withReason("Missing sessionId query parameter"));
        }

        log.info(">>> WS session CONNECTED for game: {}", sessionId);

        Flux<WebSocketMessage> outbound = broadcaster.subscribe(sessionId)
                .doOnNext(event -> log.info("<<< WS PREPARING to send event to UI for game: {}", sessionId))
                .map(objectMapper::writeValueAsString)
                .map(session::textMessage)
                .doFinally(signal -> {
                    log.warn("xxx WS session TERMINATED for game: {}. Signal: {}. DESTROYING SINK!", sessionId, signal);
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