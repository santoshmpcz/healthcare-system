package com.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HealthCareApplication {

	private static final Logger log = LoggerFactory.getLogger(HealthCareApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(HealthCareApplication.class, args);
		log.info("Health Care Application Started Successfully");
	}
}