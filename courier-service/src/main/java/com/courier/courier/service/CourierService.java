package com.courier.courier.service;

import com.courier.courier.dto.AvailabilityRequest;
import com.courier.courier.dto.CourierRequest;
import com.courier.courier.dto.CourierResponse;
import com.courier.courier.entity.Courier;
import com.courier.courier.exception.ResourceNotFoundException;
import com.courier.courier.repository.CourierRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CourierService {

	private final CourierRepository courierRepository;

	@Transactional(readOnly = true)
	public List<CourierResponse> findAll() {
		return courierRepository.findAll().stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public CourierResponse findById(Long id) {
		return toResponse(getCourier(id));
	}

	@Transactional(readOnly = true)
	public List<CourierResponse> findAvailable(String area) {
		List<Courier> couriers = StringUtils.hasText(area)
				? courierRepository.findByIsAvailableTrueAndCurrentAreaIgnoreCase(area.trim())
				: courierRepository.findByIsAvailableTrue();
		return couriers.stream().map(this::toResponse).toList();
	}

	@Transactional
	public CourierResponse create(CourierRequest request) {
		Courier courier = Courier.builder()
				.name(request.getName())
				.phone(request.getPhone())
				.vehicleType(request.getVehicleType())
				.currentArea(request.getCurrentArea())
				.isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
				.build();
		return toResponse(courierRepository.save(courier));
	}

	@Transactional
	public CourierResponse update(Long id, CourierRequest request) {
		Courier courier = getCourier(id);
		courier.setName(request.getName());
		courier.setPhone(request.getPhone());
		courier.setVehicleType(request.getVehicleType());
		courier.setCurrentArea(request.getCurrentArea());
		if (request.getIsAvailable() != null) {
			courier.setIsAvailable(request.getIsAvailable());
		}
		return toResponse(courierRepository.save(courier));
	}

	@Transactional
	public CourierResponse updateAvailability(Long id, AvailabilityRequest request) {
		Courier courier = getCourier(id);
		courier.setIsAvailable(request.getIsAvailable());
		return toResponse(courierRepository.save(courier));
	}

	@Transactional
	public void delete(Long id) {
		if (!courierRepository.existsById(id)) {
			throw new ResourceNotFoundException("Courier not found: " + id);
		}
		courierRepository.deleteById(id);
	}

	@Transactional
	public void applyAvailabilityFromEvent(Long courierId, boolean available) {
		Courier courier = courierRepository.findById(courierId)
				.orElseThrow(() -> new ResourceNotFoundException("Courier not found for event: " + courierId));
		courier.setIsAvailable(available);
		courierRepository.save(courier);
	}

	private Courier getCourier(Long id) {
		return courierRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Courier not found: " + id));
	}

	private CourierResponse toResponse(Courier courier) {
		return CourierResponse.builder()
				.id(courier.getId())
				.name(courier.getName())
				.phone(courier.getPhone())
				.vehicleType(courier.getVehicleType())
				.currentArea(courier.getCurrentArea())
				.isAvailable(courier.getIsAvailable())
				.createdAt(courier.getCreatedAt())
				.build();
	}
}
