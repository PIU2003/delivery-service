package com.courier.parcel.dto;

import com.courier.parcel.entity.ParcelStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParcelResponse {

	private Long id;
	private String senderName;
	private String senderAddress;
	private String receiverName;
	private String receiverAddress;
	private Double weight;
	private ParcelStatus status;
	private Instant createdAt;
}
