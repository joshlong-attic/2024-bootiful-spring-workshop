package com.example.web;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
class OrderService {

    private final Map<Integer, Order> orders = new ConcurrentHashMap<>();

    OrderService() {
        this.orders.putAll(Map.of(1, newOrder(1), 2, newOrder(2)));
    }

    Collection<Order> getAll() {
        return this.orders.values();
    }

    Order getById(int id) {
        return this.orders.get(id);
    }

    Order create(Order order) {
        this.orders.put(order.id(), order);
        return order;
    }

    private static Order newOrder(int id) {
        return new Order(id, UUID.randomUUID().toString(),
                (float) (Math.random() * 100));
    }


}
