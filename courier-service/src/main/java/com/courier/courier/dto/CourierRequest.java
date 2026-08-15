package com.courier.courier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierRequest {

	@NotBlank(message = "Courier name is required")
	@Size(min = 2, max = 100, message = "Courier name must be between 2 and 100 characters")
	private String name;

	@NotBlank(message = "Phone number is required")
	@Pattern(
			regexp = "^\\+?[0-9]{9,15}$",
			message = "Phone must be 9–15 digits, optionally starting with + (e.g. +94771234567)")
	private String phone;

	@NotBlank(message = "Vehicle type is required")
	@Size(min = 2, max = 64, message = "Vehicle type must be between 2 and 64 characters")
	private String vehicleType;

	@NotBlank(message = "Current area is required and cannot be blank")
	@Size(min = 2, max = 128, message = "Current area must be between 2 and 128 characters")
	private String currentArea;

	/** Optional on create; defaults to true. Used on update. */
	private Boolean isAvailable;
}
