package com.example.scalability;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootApplication
public class ScalabilityApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScalabilityApplication.class, args);
    }

    @Bean
    ApplicationRunner josePaumardsDemo() {
        return _ -> {

            var futures = new ArrayList<Future<?>>();
            var threads = new ConcurrentSkipListSet<String>();
//            var executor = Executors.newCachedThreadPool();
            var executor = Executors.newVirtualThreadPerTaskExecutor();
            for (var i = 0; i < 1000; i++) {
                var first = i == 0;
                futures.add(executor.submit(() -> {
                    sleep(threads, first);
                    sleep(threads, first);
                    sleep(threads, first);
                }));
            }

            for (var f : futures) {
                f.get();
            }
            System.out.println(threads);

        };
    }


    private void sleep(ConcurrentSkipListSet<String> threads, boolean first) {
        if (first) {
            threads.add(Thread.currentThread().toString());
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (first) {
            threads.add(Thread.currentThread().toString());
        }
    }
}

// cora iberkleid's demo
@Configuration
class DelayConfiguration {

    @Bean
    RestClient http(RestClient.Builder builder) {
        return builder.build();
    }
}

@Controller
@ResponseBody
class DelayController {

    private final RestClient http;

    DelayController(RestClient http) {
        this.http = http;
    }

    @GetMapping("/wait")
    String delay() {
        return this.http.get().uri("https://httpbin.org/delay/5").retrieve().body(String.class);
    }
}