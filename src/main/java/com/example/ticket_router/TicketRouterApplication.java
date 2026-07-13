package com.example.ticket_router;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.example.ticket_router.config.OpenAiProperties;

//includes @Configuration, @EnableAutoConfiguration, and @ComponentScan annotations, which are used to configure the Spring application context
@SpringBootApplication
//create OpenAiProperties bean and make it available for dependency injection in the application context
@EnableConfigurationProperties(OpenAiProperties.class)
public class TicketRouterApplication {
	public static void main(String[] args) {
		//run the Spring Boot application, which starts the embedded web server and initializes the application context
		SpringApplication.run(TicketRouterApplication.class, args);
	}

}
