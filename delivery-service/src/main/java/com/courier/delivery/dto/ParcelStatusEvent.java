package com.courier.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event payload published onto {@code delivery.exchange} for parcel/courier consumers.
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
