package com.example.development_desk_check;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class DevelopmentDeskCheckApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevelopmentDeskCheckApplication.class, args);
	}

	@Bean
	ApplicationRunner demo(Environment environment) {
		return _ -> System.out.println("you've been Juergenized, " + environment.getProperty("user.name")
				+ ", and your key is " + environment.getProperty("key") + "!");
	}

}
