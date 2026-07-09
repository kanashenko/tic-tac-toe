package com.tictactoe.sessionservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

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