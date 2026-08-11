package com.courier.parcel.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "parcels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parcel {

	@Id
	private String id;

	private String senderName;

	private String senderAddress;

	private String receiverName;

	private String receiverAddress;

	private Double weight;

	private ParcelStatus status;

	@CreatedDate
	private Instant createdAt;
}
