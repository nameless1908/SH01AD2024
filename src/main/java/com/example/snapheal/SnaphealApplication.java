package com.example.snapheal;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.util.Objects;

@SpringBootApplication
@EnableCaching
public class SnaphealApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().load();

		// Set environment variables
		System.setProperty("DB_USER", Objects.requireNonNull(dotenv.get("DB_USER")));
		System.setProperty("DB_PASSWORD", Objects.requireNonNull(dotenv.get("DB_PASSWORD")));
		System.setProperty("SECRET_KEY", Objects.requireNonNull(dotenv.get("SECRET_KEY")));
		System.setProperty("MAIL_USERNAME", Objects.requireNonNull(dotenv.get("MAIL_USERNAME")));
		System.setProperty("MAIL_PASSWORD", Objects.requireNonNull(dotenv.get("MAIL_PASSWORD")));
		System.setProperty("MAIL_HOST", Objects.requireNonNull(dotenv.get("MAIL_HOST")));
		System.setProperty("MAIL_PORT", Objects.requireNonNull(dotenv.get("MAIL_PORT")));
		SpringApplication.run(SnaphealApplication.class, args);
	}

}
