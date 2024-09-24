package com.example.web;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
class Clients {

    record Slide(String title, String type) {
    }

    record Slideshow(String author, String date, Slide[] slides) {
    }

    record Response(Slideshow slideshow) {
    }


    void validate(Class<?> clzz, Response response) {
        Assert.notNull(response, "response must not be null");
        Assert.notNull(response.slideshow(), "slideshow must not be null");
        Assert.notNull(response.slideshow().slides(), "slideshow slides must not be null");
        Assert.notNull(response.slideshow().slides().length > 0, "slideshow slides length must not be null");
        System.out.println("==========================================");
        System.out.println("type: " + clzz.getName());
        for (var slide : response.slideshow().slides()) {
            System.out.println("slide : " + slide.toString());
        }
    }

    @Bean
    ApplicationRunner clientRunner(RestTemplate restTemplate,
                                   RestClient restClient,
                                   SlideshowClient slideshowClient) {
        return _ -> {
            var url = "https://httpbin.org/json";

            this.validate(RestTemplate.class, restTemplate.getForObject(url, Response.class));
            this.validate(RestClient.class, restClient.get().uri(url).retrieve().body(Response.class));
            this.validate(SlideshowClient.class, slideshowClient.get());
        };
    }

    @Bean
    JdkClientHttpRequestFactory jdkClientHttpRequestFactory() {
        return new JdkClientHttpRequestFactory();
    }

    @Bean
    RestClient restClient(
            JdkClientHttpRequestFactory jdkClientHttpRequestFactory,
            RestClient.Builder builder) {
        return builder
                .requestFactory(jdkClientHttpRequestFactory)
                .build();
    }

    @Bean
    RestTemplate restTemplate(
            JdkClientHttpRequestFactory jdkClientHttpRequestFactory,
            RestTemplateBuilder builder) {
        return builder
                .requestFactory(() -> jdkClientHttpRequestFactory)
                .build();
    }

    @Bean
    SlideshowClient slideshowClient(RestClient restClient) {
        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(SlideshowClient.class);
    }
}

interface SlideshowClient {

    @GetExchange("https://httpbin.org/json")
    Clients.Response get();
}