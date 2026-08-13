package com.courier.parcel.dto;

import com.courier.parcel.entity.ParcelStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequest {

	@NotNull
	private ParcelStatus status;
}
