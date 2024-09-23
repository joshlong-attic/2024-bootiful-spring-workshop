package com.example.web;

import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

import java.text.SimpleDateFormat;
import java.util.*;

/*
some possible queries:
``` 
query {
    id(id: 2) {
        id
                date
        sku
    }

    all {
        id
                date
        sku
    }
}
```

some possible mutations:

```

mutation {
	create (id:3,sku: "2322", price:22.0)  {
    id, date ,sku
  }
}

```

*/

@Controller
class OrderGraphqlController {

    private final OrderService service;

    OrderGraphqlController(OrderService service) {
        this.service = service;
    }

    @MutationMapping
    Order create(@Argument int id,
                 @Argument String sku,
                 @Argument float price) {
        return this.service.create(
                new Order(id, sku, price));
    }

    @QueryMapping
    Collection<Order> all() {
        return this.service.getAll();
    }

    @QueryMapping
    Order id(@Argument int id) {
        return this.service.getById(id);
    }

    @SchemaMapping
    String date(Order order) {
        // get the attribute by consulting some field in the database 
        // for the order or something..
        return SimpleDateFormat.getDateInstance().format(new Date());
    }

    @BatchMapping
    Map<Order, Collection<LineItem>> lineItems(List<Order> orderList) {
        // some query to get all LineItems mapping to these Orders
        System.out.println("batching all the lineItem lookups");
        var ctr = 0;
        var map = new HashMap<Order, Collection<LineItem>>();
        for (var o : orderList)
            map.put(o, List.of(new LineItem(ctr++), new LineItem(ctr++)));
        return map;
    }
}
