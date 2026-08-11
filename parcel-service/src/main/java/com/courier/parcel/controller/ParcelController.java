package com.courier.parcel.controller;

import com.courier.parcel.dto.ParcelRequest;
import com.courier.parcel.dto.ParcelResponse;
import com.courier.parcel.dto.ParcelStatusResponse;
import com.courier.parcel.service.ParcelService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parcels")
@RequiredArgsConstructor
@Tag(name = "Parcels", description = "Parcel booking and status APIs")
@SecurityRequirement(name = "ApiKey")
public class ParcelController {

	private final ParcelService parcelService;

	@GetMapping
	@Operation(summary = "List all parcels")
	public List<ParcelResponse> list() {
		return parcelService.findAll();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get parcel by id")
	public ParcelResponse get(@PathVariable String id) {
		return parcelService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a parcel booking")
	public ParcelResponse create(@Valid @RequestBody ParcelRequest request) {
		return parcelService.create(request);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a parcel")
	public ParcelResponse update(@PathVariable String id, @Valid @RequestBody ParcelRequest request) {
		return parcelService.update(id, request);
	}

	@GetMapping("/{id}/status")
	@Operation(summary = "Get parcel status only")
	public ParcelStatusResponse status(@PathVariable String id) {
		return parcelService.getStatus(id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete a parcel")
	public void delete(@PathVariable String id) {
		parcelService.delete(id);
	}
}
