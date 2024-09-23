package com.example.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Map;

import static org.springframework.web.servlet.function.RouterFunctions.route;

@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

    @Bean
    RouterFunction<ServerResponse> httpRequest() {
        return route()
                .GET("/hello", _ -> ServerResponse.ok().body(Map.of("message", "Hello World")))
                .build();
    }
}


