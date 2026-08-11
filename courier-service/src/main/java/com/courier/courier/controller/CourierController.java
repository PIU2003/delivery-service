package com.courier.courier.controller;

import com.courier.courier.dto.AvailabilityRequest;
import com.courier.courier.dto.CourierRequest;
import com.courier.courier.dto.CourierResponse;
import com.courier.courier.service.CourierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/couriers")
@RequiredArgsConstructor
@Tag(name = "Couriers", description = "Courier profile and availability APIs")
@SecurityRequirement(name = "ApiKey")
public class CourierController {

	private final CourierService courierService;

	@GetMapping
	@Operation(summary = "List all couriers")
	public List<CourierResponse> list() {
		return courierService.findAll();
	}

	@GetMapping("/available")
	@Operation(summary = "List available couriers, optionally filtered by area")
	public List<CourierResponse> available(@RequestParam(required = false) String area) {
		return courierService.findAvailable(area);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get courier by id")
	public CourierResponse get(@PathVariable String id) {
		return courierService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a courier")
	public CourierResponse create(@Valid @RequestBody CourierRequest request) {
		return courierService.create(request);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a courier")
	public CourierResponse update(@PathVariable String id, @Valid @RequestBody CourierRequest request) {
		return courierService.update(id, request);
	}

	@PutMapping("/{id}/availability")
	@Operation(summary = "Update courier availability")
	public CourierResponse updateAvailability(
			@PathVariable String id, @Valid @RequestBody AvailabilityRequest request) {
		return courierService.updateAvailability(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete a courier")
	public void delete(@PathVariable String id) {
		courierService.delete(id);
	}
}
