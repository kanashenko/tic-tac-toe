package com.tictactoe.sessionservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Provides the {@link WebClient} used by {@link com.tictactoe.sessionservice.client.GameEngineClient}.
 * The load-balancer exchange filter resolves {@code game-engine.url}'s host
 * (e.g. {@code http://game-engine}) as a Eureka service ID and picks a live
 * instance, so calls keep working as Game Engine instances scale or restart.
 */
@Configuration
public class GameEngineClientConfig {

    @Bean
    public WebClient gameEngineWebClient(
            @Value("${game-engine.url}") String baseUrl,
            ReactorLoadBalancerExchangeFilterFunction lbFunction) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .filter(lbFunction)
                .build();
    }
}
