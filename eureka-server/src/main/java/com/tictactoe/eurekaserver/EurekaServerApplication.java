package com.tictactoe.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Service registry for the stack: every other service registers itself here
 * and discovers its peers through it, standing in for a production service
 * mesh / discovery solution.
 */
@EnableEurekaServer
@SpringBootApplication
class EurekaServerApplication {

    static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }

}
