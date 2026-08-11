package com.courier.delivery.controller;

import com.courier.delivery.dto.AssignDeliveryRequest;
import com.courier.delivery.dto.DeliveryResponse;
import com.courier.delivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Tag(name = "Deliveries", description = "Delivery orchestration APIs")
@SecurityRequirement(name = "ApiKey")
public class DeliveryController {

	private final DeliveryService deliveryService;

	@GetMapping
	@Operation(summary = "List all deliveries")
	public List<DeliveryResponse> list() {
		return deliveryService.findAll();
	}

	@GetMapping("/track/{parcelId}")
	@Operation(summary = "Track delivery by parcel id")
	public DeliveryResponse track(@PathVariable String parcelId) {
		return deliveryService.trackByParcelId(parcelId);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get delivery by id")
	public DeliveryResponse get(@PathVariable String id) {
		return deliveryService.findById(id);
	}

	@PostMapping("/assign")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Assign an available courier to a parcel and publish parcel.assigned")
	public DeliveryResponse assign(@Valid @RequestBody AssignDeliveryRequest request) {
		return deliveryService.assign(request);
	}

	@PutMapping("/{id}/pickup")
	@Operation(summary = "Mark delivery as picked up and publish parcel.pickedup")
	public DeliveryResponse pickup(@PathVariable String id) {
		return deliveryService.pickup(id);
	}

	@PutMapping("/{id}/complete")
	@Operation(summary = "Mark delivery as delivered and publish parcel.delivered")
	public DeliveryResponse complete(@PathVariable String id) {
		return deliveryService.complete(id);
	}
}
