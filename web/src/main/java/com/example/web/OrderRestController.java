package com.example.web;

import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Controller
@ResponseBody
@RequestMapping("/orders")
class OrderRestController {


    private final OrderService service;

    OrderRestController(OrderService service) {
        this.service = service;
    }

    // curl http://localhost:8080/orders
    @GetMapping
    Collection<Order> getAll() {
        return this.service.getAll();
    }

    // curl http://localhost:8080/orders/1
    @GetMapping("/{id}")
    Order getById(@PathVariable int id) {
        return this.service.getById(id);
    }

    // curl -XPOST -H"content-type: application/json" -d'{"sku":"12345" , "id":3, "price": 2322.0}' http://localhost:8080/orders
    @PostMapping
    ResponseEntity<?> create(@RequestBody Order order) {
        this.service.create(order);
        return ResponseEntity.ok().build();
    }
    
    
    
}

