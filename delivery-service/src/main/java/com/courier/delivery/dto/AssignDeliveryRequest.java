package com.courier.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignDeliveryRequest {

	@NotBlank(message = "parcelId is required")
	@Size(min = 1, max = 64, message = "parcelId must be a valid document id")
	private String parcelId;

	@NotBlank(message = "area is required")
	@Size(min = 2, max = 128, message = "area must be between 2 and 128 characters")
	private String area;
}
