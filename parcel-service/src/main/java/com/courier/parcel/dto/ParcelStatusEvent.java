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

	private Long parcelId;
	private Long courierId;
	private Long deliveryId;
}
