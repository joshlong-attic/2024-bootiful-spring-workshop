package com.example.basics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;

@SpringBootApplication
public class BasicsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicsApplication.class, args);
    }

}

@Controller
@ResponseBody
class CustomerController {

    private final JdbcClient db;

    CustomerController(JdbcClient db) {
        this.db = db;
    }
 
    @GetMapping ("/hello")
    String hello (){
        // refactor and rebuild
        return "Hello World!" ;
    }
    
    @GetMapping("/customers")
    Collection<Customer> customers() {
        return this.db
                .sql("select * from customer")
                .query((rs, _) -> new Customer(rs.getInt("id"),
                        rs.getString("name")))
                .list();
    }
}

record Customer(int id, String name) {
}
