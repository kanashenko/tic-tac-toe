package com.tictactoe.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The single public entry point for the UI and API clients. Routes are
 * declared in {@code application.yaml} and resolved against the Eureka registry
 */
@SpringBootApplication
class GatewayApplication {

    static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
