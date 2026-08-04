package com.courier.parcel.dto;

import com.courier.parcel.entity.ParcelStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParcelStatusResponse {

	private Long id;
	private ParcelStatus status;
}
