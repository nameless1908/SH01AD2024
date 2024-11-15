package com.example.snapheal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SnaphealApplication {

	public static void main(String[] args) {
		SpringApplication.run(SnaphealApplication.class, args);
	}

}
