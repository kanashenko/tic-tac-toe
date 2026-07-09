package com.tictactoe.gameengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Game Engine microservice entry point. Owns Tic Tac Toe board state, move
 * validation, and win/draw detection behind a REST API — see
 * {@link com.tictactoe.gameengine.controller.GameController}.
 */
@SpringBootApplication
class GameEngineApplication {

    static void main(String[] args) {
        SpringApplication.run(GameEngineApplication.class, args);
    }

}
