package com.tpi.solicitudes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${ms-logistica.url:http://localhost:8081}")
    private String msLogisticaUrl;

    //@Bean
    //public WebClient webClientLogistica(WebClient.Builder builder) {
    //    return builder
    //            .baseUrl(msLogisticaUrl)
    //            .build();
    //}
}
