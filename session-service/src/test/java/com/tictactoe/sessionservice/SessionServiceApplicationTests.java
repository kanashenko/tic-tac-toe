package com.tictactoe.sessionservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Eureka client disabled so the context-load test doesn't retry registration against an unresolvable host.
@SpringBootTest(properties = "eureka.client.enabled=false")
class SessionServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
