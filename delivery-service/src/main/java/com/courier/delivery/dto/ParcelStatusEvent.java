package com.courier.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Delivery Service orchestration event published onto {@code delivery.exchange}.
 * Consumed by Parcel Service (status) and Courier Service (availability).
 * Source of truth for the run: {@code deliveryId}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParcelStatusEvent {

	private String parcelId;
	private String courierId;
	private String deliveryId;
	/** DeliveryStatus name: ASSIGNED, PICKED_UP, DELIVERED */
	private String status;
}
