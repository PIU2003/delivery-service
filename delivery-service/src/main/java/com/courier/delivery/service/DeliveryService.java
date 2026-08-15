package com.courier.delivery.service;

import com.courier.delivery.client.CourierServiceClient;
import com.courier.delivery.client.ParcelServiceClient;
import com.courier.delivery.dto.AssignDeliveryRequest;
import com.courier.delivery.dto.CourierDto;
import com.courier.delivery.dto.DeliveryResponse;
import com.courier.delivery.entity.Delivery;
import com.courier.delivery.entity.DeliveryStatus;
import com.courier.delivery.exception.ResourceNotFoundException;
import com.courier.delivery.messaging.DeliveryEventPublisher;
import com.courier.delivery.repository.DeliveryRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

	private static final List<DeliveryStatus> ACTIVE_STATUSES =
			List.of(DeliveryStatus.ASSIGNED, DeliveryStatus.PICKED_UP);

	private final DeliveryRepository deliveryRepository;
	private final CourierServiceClient courierServiceClient;
	private final ParcelServiceClient parcelServiceClient;
	private final DeliveryEventPublisher eventPublisher;

	public List<DeliveryResponse> findAll() {
		return deliveryRepository.findAll().stream().map(this::toResponse).toList();
	}

	public List<DeliveryResponse> findActive() {
		return deliveryRepository.findByStatusInOrderByAssignedAtDesc(ACTIVE_STATUSES).stream()
				.map(this::toResponse)
				.toList();
	}

	public DeliveryResponse findById(String id) {
		return toResponse(getDelivery(id));
	}

	public DeliveryResponse trackByParcelId(String parcelId) {
		if (!StringUtils.hasText(parcelId)) {
			throw new IllegalArgumentException("Delivery Service: parcelId is required for tracking");
<<<<<<< HEAD
=======
		}
		List<Delivery> deliveries =
				deliveryRepository.findByParcelIdOrderByAssignedAtDesc(parcelId.trim());
		if (deliveries.isEmpty()) {
			throw new ResourceNotFoundException(
					"Delivery Service: no delivery run found for parcelId=" + parcelId.trim());
>>>>>>> fd1c4600ae4d14aceeeda243106fa8924781c0f2
		}
		List<Delivery> deliveries =
				deliveryRepository.findByParcelIdOrderByAssignedAtDesc(parcelId.trim());
		if (deliveries.isEmpty()) {
			throw new ResourceNotFoundException(
					"Delivery Service: no delivery run found for parcelId=" + parcelId.trim());
		}
		Delivery latest = deliveries.getFirst();
		// Keep parcel status aligned with the delivery run (repairs stale PENDING rows)
		syncParcelStatus(latest);
		return toResponse(latest);
	}

	public DeliveryResponse assign(AssignDeliveryRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Delivery Service: assign request body is required");
		}
		String parcelId = request.getParcelId() == null ? "" : request.getParcelId().trim();
		String area = request.getArea() == null ? "" : request.getArea().trim();
		if (!StringUtils.hasText(parcelId)) {
			throw new IllegalArgumentException("Delivery Service: parcelId is required before assign");
		}
		if (!StringUtils.hasText(area)) {
			throw new IllegalArgumentException("Delivery Service: area is required before assign");
		}

		deliveryRepository
				.findFirstByParcelIdAndStatusInOrderByAssignedAtDesc(parcelId, ACTIVE_STATUSES)
				.ifPresent(existing -> {
					throw new IllegalStateException(
							"Delivery Service: cannot assign — parcelId="
									+ parcelId
									+ " already has an active run (deliveryId="
									+ existing.getId()
									+ ", status="
									+ existing.getStatus()
									+ ")");
<<<<<<< HEAD
				});

		deliveryRepository.findByParcelIdOrderByAssignedAtDesc(parcelId).stream()
				.findFirst()
				.filter(existing -> existing.getStatus() == DeliveryStatus.DELIVERED)
				.ifPresent(existing -> {
					throw new IllegalStateException(
							"Delivery Service: cannot assign — parcelId="
									+ parcelId
									+ " was already delivered (deliveryId="
									+ existing.getId()
									+ ")");
=======
>>>>>>> fd1c4600ae4d14aceeeda243106fa8924781c0f2
				});

		List<CourierDto> available = courierServiceClient.findAvailable(area);
		if (available.isEmpty()) {
			throw new IllegalStateException(
					"Delivery Service: no available couriers in area \""
							+ area
							+ "\" — free a courier or choose another area");
		}

		CourierDto courier = available.getFirst();
		if (!StringUtils.hasText(courier.getId())) {
			throw new IllegalStateException(
					"Delivery Service: courier-service returned a courier without an id for area=" + area);
		}

		Delivery delivery = Delivery.builder()
				.parcelId(parcelId)
				.courierId(courier.getId())
				.area(area)
				.status(DeliveryStatus.ASSIGNED)
				.assignedAt(Instant.now())
				.build();

		Delivery saved = deliveryRepository.save(delivery);
		eventPublisher.publishAssigned(saved);
		syncParcelStatus(saved);
		return toResponse(saved);
	}

	public DeliveryResponse pickup(String id) {
		if (!StringUtils.hasText(id)) {
			throw new IllegalArgumentException("Delivery Service: deliveryId is required for pickup");
		}
		Delivery delivery = getDelivery(id.trim());
		if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
			throw new IllegalStateException(
					"Delivery Service: pickup rejected for deliveryId="
							+ delivery.getId()
							+ " — expected status ASSIGNED but was "
							+ delivery.getStatus()
							+ " (parcelId="
							+ delivery.getParcelId()
							+ ")");
		}
		delivery.setStatus(DeliveryStatus.PICKED_UP);
		delivery.setPickedUpAt(Instant.now());
		Delivery saved = deliveryRepository.save(delivery);
		eventPublisher.publishPickedUp(saved);
		syncParcelStatus(saved);
		return toResponse(saved);
	}

	public DeliveryResponse complete(String id) {
		if (!StringUtils.hasText(id)) {
			throw new IllegalArgumentException("Delivery Service: deliveryId is required for complete");
		}
		Delivery delivery = getDelivery(id.trim());
		if (delivery.getStatus() != DeliveryStatus.PICKED_UP) {
			throw new IllegalStateException(
					"Delivery Service: complete rejected for deliveryId="
							+ delivery.getId()
							+ " — expected status PICKED_UP but was "
							+ delivery.getStatus()
							+ " (parcelId="
							+ delivery.getParcelId()
							+ ")");
		}
		delivery.setStatus(DeliveryStatus.DELIVERED);
		delivery.setDeliveredAt(Instant.now());
		Delivery saved = deliveryRepository.save(delivery);
		eventPublisher.publishDelivered(saved);
		syncParcelStatus(saved);
		return toResponse(saved);
	}

	private void syncParcelStatus(Delivery delivery) {
		String parcelStatus = switch (delivery.getStatus()) {
			case ASSIGNED -> "ASSIGNED";
			case PICKED_UP -> "IN_TRANSIT";
			case DELIVERED -> "DELIVERED";
		};
		try {
			parcelServiceClient.updateStatus(delivery.getParcelId(), parcelStatus);
		} catch (Exception ex) {
			log.warn(
					"Parcel status sync failed for parcelId={} targetStatus={}: {}",
					delivery.getParcelId(),
					parcelStatus,
					ex.getMessage());
		}
	}

	private Delivery getDelivery(String id) {
		return deliveryRepository
				.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Delivery Service: delivery not found for id=" + id));
	}

	private DeliveryResponse toResponse(Delivery delivery) {
		return DeliveryResponse.builder()
				.id(delivery.getId())
				.parcelId(delivery.getParcelId())
				.courierId(delivery.getCourierId())
				.area(delivery.getArea())
				.status(delivery.getStatus())
				.assignedAt(delivery.getAssignedAt())
				.pickedUpAt(delivery.getPickedUpAt())
				.deliveredAt(delivery.getDeliveredAt())
				.build();
	}
}
