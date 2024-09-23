package com.example.data;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.Set;

@SpringBootApplication
public class DataApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(CustomerRepository repository) {
        return _ -> repository.findAll().forEach(System.out::println);
    }
}

interface CustomerRepository extends CrudRepository<Customer, Integer> {
}

record LineItem(@Id int id, String sku , int customer) {
}

record Customer(@Id int id, String name, Set<LineItem> lineItems) {
} 