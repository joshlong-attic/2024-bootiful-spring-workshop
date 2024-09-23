package com.example.service_b;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@SpringBootApplication
public class ServiceBApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceBApplication.class, args);
    }

}

@Controller
@ResponseBody
class ProducerController {
    
    private final String value;

    ProducerController(@Value("${message}") String value) {
        this.value = value;
    }

    @GetMapping("/answer")
    Map<String, String> answer() {
        return Map.of("message", this.value);
    }
}