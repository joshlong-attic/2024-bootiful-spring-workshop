package com.example.modularity.adoptions;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@Controller
class DogAdoptionController {

    private final DogAdoptionService service;

    DogAdoptionController(DogAdoptionService service) {
        this.service = service;
    }

    @PostMapping("/dogs/{dogId}/adoptions")
    void adopt(@PathVariable int dogId, Map<String, String> owner) {
        this.service.adopt(dogId, owner.get("name"));
    }
}


@Service
@Transactional
class DogAdoptionService {

    private final DogRepository repository;
    private final ApplicationEventPublisher publisher;

    DogAdoptionService(DogRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    void adopt(int dogId, String ownerName) {
        this.repository.findById(dogId).ifPresent(dog -> {
            var newDog = this.repository.save(new Dog(dog.id(),
                    ownerName, dog.name()));
            System.out.println("adopted [" + newDog + "]");
            this.publisher.publishEvent(new DogAdoptionEvent(dog.id()));
        });
    }
}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

record Dog(@Id int id, String owner, String name) {
}