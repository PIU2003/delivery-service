package com.courier.parcel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class ParcelServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParcelServiceApplication.class, args);
	}

}
