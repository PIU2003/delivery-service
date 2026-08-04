package com.courier.courier.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierRequest {

	@NotBlank
	private String name;

	@NotBlank
	private String phone;

	@NotBlank
	private String vehicleType;

	@NotBlank
	private String currentArea;

	/** Optional on create; defaults to true. Used on update. */
	private Boolean isAvailable;
}
