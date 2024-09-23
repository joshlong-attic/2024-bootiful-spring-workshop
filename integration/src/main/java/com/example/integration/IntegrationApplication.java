package com.example.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.file.dsl.Files;

import java.io.File;

@SpringBootApplication
public class IntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationApplication.class, args);
    }

    @Bean
    IntegrationFlow newFilesIntegrationFlow(@Value("file://${HOME}/Desktop/inbound") File in,
                                            @Value("file://${HOME}/Desktop/outbound") File out) {
        return IntegrationFlow
                .from(Files.inboundAdapter(in).autoCreateDirectory(true))
                .handle((GenericHandler<File>) (payload, _) -> {
                    System.out.println("got a new file called " + payload.getAbsolutePath() + '.');
                    return payload;
                })
                .handle(Files.outboundAdapter(out).autoCreateDirectory(true))
                .get();
    }
}
