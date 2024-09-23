package com.example.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

@SpringBootApplication
@RegisterReflectionForBinding ( DogAdoptionSuggestion.class)
public class AiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }

    @Bean
    ApplicationRunner demo(ChatClient cc) {
        return _ -> {
            var content = cc
                    .prompt()
                    .user("do you have any neurotic dogs?")
                    .call()
                    .entity( DogAdoptionSuggestion.class);
            System.out.println("content [" + content + "]");
        };
    }

    @Bean
    ChatClient chatClient(
            ChatClient.Builder builder,
            @Value("classpath:/my-system-prompt.md") Resource prompt,
            DogRepository repository, VectorStore vectorStore) {

        if (false)
        repository.findAll().forEach(dog -> {
            var dogument = new Document("id: %s, name: %s, description: %s".formatted(
                    dog.id(), dog.name(), dog.description()
            ));
            vectorStore.add(List.of(dogument));
        });

        return builder
                .defaultSystem(prompt)
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
    }
}

record Dog(@Id int id, String name, String description) {
}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

record DogAdoptionSuggestion (int id, String name, String description) {}