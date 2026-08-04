package com.courier.delivery.entity;

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
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long parcelId;

	@Column(nullable = false)
	private Long courierId;

	@Column(nullable = false, length = 128)
	private String area;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private DeliveryStatus status;

	@Column(nullable = false, updatable = false)
	private Instant assignedAt;

	private Instant pickedUpAt;

	private Instant deliveredAt;

	@PrePersist
	void onCreate() {
		if (assignedAt == null) {
			assignedAt = Instant.now();
		}
		if (status == null) {
			status = DeliveryStatus.ASSIGNED;
		}
	}
}
