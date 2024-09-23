package com.example.modularity.vet;

import com.example.modularity.adoptions.DogAdoptionEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class Dogtor {
    
    @EventListener
    void dogAdoptedEvent (DogAdoptionEvent dogAdoptionEvent) {
        System.out.println("checking up on the health of the dog [" +
                dogAdoptionEvent +"]");
    }
}
