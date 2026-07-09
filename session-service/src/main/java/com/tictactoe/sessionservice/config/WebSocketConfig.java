package com.tictactoe.sessionservice.config;

import com.tictactoe.sessionservice.websocket.SessionWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;

import java.util.Map;

/** Wires {@link SessionWebSocketHandler} to the {@code /ws/sessions} path. */
@Configuration
@RequiredArgsConstructor
public class WebSocketConfig {

    private final SessionWebSocketHandler handler;

    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(Map.of("/ws/sessions", handler));
        mapping.setOrder(-1);
        return mapping;
    }
}
