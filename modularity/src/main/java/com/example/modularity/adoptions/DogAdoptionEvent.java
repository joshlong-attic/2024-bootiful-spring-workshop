package com.example.modularity.adoptions;

import org.springframework.modulith.events.Externalized;

@Externalized
public record DogAdoptionEvent (int dogId) {
}
