package com.courier.delivery.dto;

import com.courier.delivery.entity.DeliveryStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryResponse {

	private Long id;
	private Long parcelId;
	private Long courierId;
	private String area;
	private DeliveryStatus status;
	private Instant assignedAt;
	private Instant pickedUpAt;
	private Instant deliveredAt;
}
