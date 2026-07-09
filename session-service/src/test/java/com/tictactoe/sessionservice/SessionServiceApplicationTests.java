package com.tictactoe.sessionservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Eureka client disabled: there's no registry to reach in a plain context-load
// test, and without this the test spends its whole run retrying registration
// against an unresolvable "eureka-server" host.
@SpringBootTest(properties = "eureka.client.enabled=false")
class SessionServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
