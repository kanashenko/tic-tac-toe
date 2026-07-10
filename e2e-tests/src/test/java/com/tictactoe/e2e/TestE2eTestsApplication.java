package com.tictactoe.e2e;

import com.tictactoe.e2etests.E2eTestsApplication;
import org.springframework.boot.SpringApplication;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestE2eTestsApplication {

    static void main(String[] args) {
        SpringApplication.from(E2eTestsApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
