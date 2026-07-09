package com.tictactoe.sessionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Session Service microservice entry point. Owns session/move-history
 * persistence and automates gameplay by driving the Game Engine — see
 * {@link com.tictactoe.sessionservice.service.SimulationService} — while
 * streaming progress to the UI over WebSocket.
 */
@SpringBootApplication
class SessionServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(SessionServiceApplication.class, args);
    }

}
