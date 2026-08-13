package com.courier.parcel.service;

import com.courier.parcel.dto.ParcelRequest;
import com.courier.parcel.dto.ParcelResponse;
import com.courier.parcel.dto.ParcelStatusResponse;
import com.courier.parcel.entity.Parcel;
import com.courier.parcel.entity.ParcelStatus;
import com.courier.parcel.exception.ResourceNotFoundException;
import com.courier.parcel.repository.ParcelRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParcelService {

	private final ParcelRepository parcelRepository;

	public List<ParcelResponse> findAll() {
		return parcelRepository.findAll().stream().map(this::toResponse).toList();
	}

	public ParcelResponse findById(String id) {
		return toResponse(getParcel(id));
	}

	public ParcelStatusResponse getStatus(String id) {
		Parcel parcel = getParcel(id);
		return ParcelStatusResponse.builder().id(parcel.getId()).status(parcel.getStatus()).build();
	}

	public ParcelStatusResponse updateStatus(String id, ParcelStatus status) {
		applyStatusFromEvent(id, status);
		return getStatus(id);
	}

	public ParcelResponse create(ParcelRequest request) {
		Parcel parcel = Parcel.builder()
				.senderName(request.getSenderName())
				.senderAddress(request.getSenderAddress())
				.receiverName(request.getReceiverName())
				.receiverAddress(request.getReceiverAddress())
				.weight(request.getWeight())
				.status(ParcelStatus.PENDING)
				.build();
		return toResponse(parcelRepository.save(parcel));
	}

	public ParcelResponse update(String id, ParcelRequest request) {
		Parcel parcel = getParcel(id);
		parcel.setSenderName(request.getSenderName());
		parcel.setSenderAddress(request.getSenderAddress());
		parcel.setReceiverName(request.getReceiverName());
		parcel.setReceiverAddress(request.getReceiverAddress());
		parcel.setWeight(request.getWeight());
		if (request.getStatus() != null) {
			parcel.setStatus(request.getStatus());
		}
		return toResponse(parcelRepository.save(parcel));
	}

	public void delete(String id) {
		if (!parcelRepository.existsById(id)) {
			throw new ResourceNotFoundException("Parcel not found: " + id);
		}
		parcelRepository.deleteById(id);
	}

	public void applyStatusFromEvent(String parcelId, ParcelStatus status) {
		Parcel parcel = parcelRepository.findById(parcelId)
				.orElseThrow(() -> new ResourceNotFoundException("Parcel not found for event: " + parcelId));
		// Never move a parcel backwards (e.g. late assigned after delivered)
		if (parcel.getStatus() != null && rank(parcel.getStatus()) > rank(status)) {
			return;
		}
		parcel.setStatus(status);
		parcelRepository.save(parcel);
	}

	private static int rank(ParcelStatus status) {
		return switch (status) {
			case PENDING -> 0;
			case ASSIGNED -> 1;
			case IN_TRANSIT -> 2;
			case DELIVERED -> 3;
			case CANCELLED -> -1;
		};
	}

	private Parcel getParcel(String id) {
		return parcelRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Parcel not found: " + id));
	}

	private ParcelResponse toResponse(Parcel parcel) {
		return ParcelResponse.builder()
				.id(parcel.getId())
				.senderName(parcel.getSenderName())
				.senderAddress(parcel.getSenderAddress())
				.receiverName(parcel.getReceiverName())
				.receiverAddress(parcel.getReceiverAddress())
				.weight(parcel.getWeight())
				.status(parcel.getStatus())
				.createdAt(parcel.getCreatedAt())
				.build();
	}
}
