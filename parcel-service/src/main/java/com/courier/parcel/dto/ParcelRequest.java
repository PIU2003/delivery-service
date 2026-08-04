package com.courier.parcel.dto;

import com.courier.parcel.entity.ParcelStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParcelRequest {

	@NotBlank
	private String senderName;

	@NotBlank
	private String senderAddress;

	@NotBlank
	private String receiverName;

	@NotBlank
	private String receiverAddress;

	@NotNull
	@Positive
	private Double weight;

	/** Optional on create; ignored defaults to PENDING. Used on update. */
	private ParcelStatus status;
}
