package org.example.chatservice.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.http.WebSocket;

@Configuration
public class LlmConfig {
    @Value("${gemini.api.url}")
    private String baseUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Bean
    public WebClient webClient(){
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}
