package com.courier.parcel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "parcels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parcel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String senderName;

	@Column(nullable = false, length = 512)
	private String senderAddress;

	@Column(nullable = false)
	private String receiverName;

	@Column(nullable = false, length = 512)
	private String receiverAddress;

	@Column(nullable = false)
	private Double weight;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ParcelStatus status;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
		if (status == null) {
			status = ParcelStatus.PENDING;
		}
	}
}
