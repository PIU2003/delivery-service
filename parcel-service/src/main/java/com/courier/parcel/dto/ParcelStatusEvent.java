package com.courier.parcel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event payload published by delivery-service onto {@code delivery.exchange}.
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
