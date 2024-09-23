package com.example.service_a;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;

import java.util.Map;


@SpringBootApplication
public class ServiceAApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceAApplication.class, args);
    }

    @Bean
    ApplicationRunner configClientRunner(Environment environment) {
        return _ -> System.out.println("message: [" + environment.getProperty("message") + "]");
    }

    @Bean
    @LoadBalanced
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }
}

@Controller
@ResponseBody
class ConsumerController {

    private final RestClient http;

    ConsumerController(RestClient http) {
        this.http = http;
    }

    @GetMapping("/call")
    Map<String, Object> call() {
        return this.http
                .get()
                .uri("http://service-b/answer")
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}