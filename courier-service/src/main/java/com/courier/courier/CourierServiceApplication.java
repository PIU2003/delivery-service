package com.courier.courier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class CourierServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CourierServiceApplication.class, args);
	}

}
