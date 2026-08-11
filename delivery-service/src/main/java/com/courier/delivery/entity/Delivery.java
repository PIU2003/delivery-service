package com.courier.delivery.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

	@Id
	private String id;

	private String parcelId;

	private String courierId;

	private String area;

	private DeliveryStatus status;

	private Instant assignedAt;

	private Instant pickedUpAt;

	private Instant deliveredAt;
}
