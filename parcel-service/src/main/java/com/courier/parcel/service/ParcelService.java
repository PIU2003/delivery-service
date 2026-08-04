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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParcelService {

	private final ParcelRepository parcelRepository;

	@Transactional(readOnly = true)
	public List<ParcelResponse> findAll() {
		return parcelRepository.findAll().stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public ParcelResponse findById(Long id) {
		return toResponse(getParcel(id));
	}

	@Transactional(readOnly = true)
	public ParcelStatusResponse getStatus(Long id) {
		Parcel parcel = getParcel(id);
		return ParcelStatusResponse.builder().id(parcel.getId()).status(parcel.getStatus()).build();
	}

	@Transactional
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

	@Transactional
	public ParcelResponse update(Long id, ParcelRequest request) {
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

	@Transactional
	public void delete(Long id) {
		if (!parcelRepository.existsById(id)) {
			throw new ResourceNotFoundException("Parcel not found: " + id);
		}
		parcelRepository.deleteById(id);
	}

	@Transactional
	public void applyStatusFromEvent(Long parcelId, ParcelStatus status) {
		Parcel parcel = parcelRepository.findById(parcelId)
				.orElseThrow(() -> new ResourceNotFoundException("Parcel not found for event: " + parcelId));
		parcel.setStatus(status);
		parcelRepository.save(parcel);
	}

	private Parcel getParcel(Long id) {
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
