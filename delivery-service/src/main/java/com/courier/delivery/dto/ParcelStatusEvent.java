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

	private String parcelId;
	private String courierId;
	private String deliveryId;
}
