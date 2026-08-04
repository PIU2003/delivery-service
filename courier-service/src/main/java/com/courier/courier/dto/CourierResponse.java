package com.courier.courier.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierResponse {

	private Long id;
	private String name;
	private String phone;
	private String vehicleType;
	private String currentArea;
	private Boolean isAvailable;
	private Instant createdAt;
}
