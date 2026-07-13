package com.example.ticket_router.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

//tells us that this class contains bean definitions and configuration settings for the Spring application context
@Configuration
public class OpenAiConfig {

    @Bean
    public WebClient openAiWebClient(OpenAiProperties openAiProperties) {
        //create a WebClient bean that can be used to make HTTP requests to the OpenAI API, using the base URL and API key from the OpenAiProperties bean
        return WebClient.builder()
                //set the base URL for the WebClient to the value of the baseUrl property from the OpenAiProperties bean
                .baseUrl(openAiProperties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + openAiProperties.apiKey())
                .build();
    }
    
}
