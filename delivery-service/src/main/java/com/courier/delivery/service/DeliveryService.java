package com.courier.delivery.service;

import com.courier.delivery.client.CourierServiceClient;
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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryService {

	private static final List<DeliveryStatus> ACTIVE_STATUSES =
			List.of(DeliveryStatus.ASSIGNED, DeliveryStatus.PICKED_UP);

	private final DeliveryRepository deliveryRepository;
	private final CourierServiceClient courierServiceClient;
	private final DeliveryEventPublisher eventPublisher;

	public List<DeliveryResponse> findAll() {
		return deliveryRepository.findAll().stream().map(this::toResponse).toList();
	}

	public DeliveryResponse findById(String id) {
		return toResponse(getDelivery(id));
	}

	public DeliveryResponse trackByParcelId(String parcelId) {
		List<Delivery> deliveries = deliveryRepository.findByParcelIdOrderByAssignedAtDesc(parcelId);
		if (deliveries.isEmpty()) {
			throw new ResourceNotFoundException("No delivery found for parcel: " + parcelId);
		}
		return toResponse(deliveries.getFirst());
	}

	public DeliveryResponse assign(AssignDeliveryRequest request) {
		deliveryRepository
				.findFirstByParcelIdAndStatusInOrderByAssignedAtDesc(request.getParcelId(), ACTIVE_STATUSES)
				.ifPresent(existing -> {
					throw new IllegalStateException(
							"Parcel " + request.getParcelId() + " already has an active delivery: " + existing.getId());
				});

		String area = request.getArea().trim();
		List<CourierDto> available = courierServiceClient.findAvailable(area);
		if (available.isEmpty()) {
			throw new IllegalStateException("No available couriers in area: " + area);
		}

		CourierDto courier = available.getFirst();
		Delivery delivery = Delivery.builder()
				.parcelId(request.getParcelId())
				.courierId(courier.getId())
				.area(area)
				.status(DeliveryStatus.ASSIGNED)
				.assignedAt(Instant.now())
				.build();

		Delivery saved = deliveryRepository.save(delivery);
		eventPublisher.publishAssigned(saved);
		return toResponse(saved);
	}

	public DeliveryResponse pickup(String id) {
		Delivery delivery = getDelivery(id);
		if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
			throw new IllegalStateException(
					"Delivery " + id + " must be ASSIGNED to pickup (current: " + delivery.getStatus() + ")");
		}
		delivery.setStatus(DeliveryStatus.PICKED_UP);
		delivery.setPickedUpAt(Instant.now());
		Delivery saved = deliveryRepository.save(delivery);
		eventPublisher.publishPickedUp(saved);
		return toResponse(saved);
	}

	public DeliveryResponse complete(String id) {
		Delivery delivery = getDelivery(id);
		if (delivery.getStatus() != DeliveryStatus.PICKED_UP) {
			throw new IllegalStateException(
					"Delivery " + id + " must be PICKED_UP to complete (current: " + delivery.getStatus() + ")");
		}
		delivery.setStatus(DeliveryStatus.DELIVERED);
		delivery.setDeliveredAt(Instant.now());
		Delivery saved = deliveryRepository.save(delivery);
		eventPublisher.publishDelivered(saved);
		return toResponse(saved);
	}

	private Delivery getDelivery(String id) {
		return deliveryRepository
				.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Delivery not found: " + id));
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
