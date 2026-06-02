package com.example.cfchat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.http.HttpClient;

/**
 * vPAIS / vLLM OpenAI-compatible endpoints reject HTTP/2 upgrade (h2c) requests.
 * Spring's default Java HTTP client negotiates h2c, which surfaces as 400 Bad Request on streaming chat.
 */
@Configuration
@Profile("cloud")
public class CloudGenAiHttpConfig {

    @Bean
    @Primary
    public RestClient.Builder genAiRestClientBuilder() {
        return RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient()));
    }

    @Bean
    @Primary
    public WebClient.Builder genAiWebClientBuilder() {
        return WebClient.builder()
                .clientConnector(new JdkClientHttpConnector(httpClient()));
    }

    static HttpClient httpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }
}
