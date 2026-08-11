package com.courier.courier.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "couriers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Courier {

	@Id
	private String id;

	private String name;

	private String phone;

	private String vehicleType;

	private String currentArea;

	private Boolean isAvailable;

	@CreatedDate
	private Instant createdAt;
}
