package com.example.snapheal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.example.snapheal.entity")
@EnableJpaRepositories("com.example.snapheal.repository")
public class SnaphealApplication {

	public static void main(String[] args) {
		SpringApplication.run(SnaphealApplication.class, args);
	}

}
