package com.courier.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Subset of courier-service response used when selecting an available courier. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierDto {

	private Long id;
	private String name;
	private String phone;
	private String vehicleType;
	private String currentArea;
	private Boolean isAvailable;
}
