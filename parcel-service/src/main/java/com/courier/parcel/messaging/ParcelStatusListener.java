package com.courier.parcel.messaging;

import com.courier.parcel.dto.ParcelStatusEvent;
import com.courier.parcel.entity.ParcelStatus;
import com.courier.parcel.exception.ResourceNotFoundException;
import com.courier.parcel.service.ParcelService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class ParcelStatusListener {

	private final ParcelService parcelService;
	private final ObjectMapper objectMapper;

	@RabbitListener(queues = "${app.rabbitmq.queue}")
	public void onStatusEvent(
			Message message, @Header(name = AmqpHeaders.RECEIVED_ROUTING_KEY, required = false) String routingKey) {
		ParcelStatusEvent event;
		try {
			event = parseEvent(message.getBody());
		} catch (Exception ex) {
			log.warn("Failed to parse parcel status event (routingKey={}): {}", routingKey, ex.getMessage());
			return;
		}

		if (!StringUtils.hasText(event.getParcelId())) {
			log.warn("Ignoring parcel status event with missing parcelId (routingKey={})", routingKey);
			return;
		}

		ParcelStatus newStatus = mapStatus(routingKey, event.getStatus());
		if (newStatus == null) {
			log.warn(
					"Ignoring unknown status mapping routingKey={} eventStatus={} for parcel {}",
					routingKey,
					event.getStatus(),
					event.getParcelId());
			return;
		}

		try {
			parcelService.applyStatusFromEvent(event.getParcelId().trim(), newStatus);
			log.info(
					"Parcel {} status updated to {} via routingKey={} eventStatus={}",
					event.getParcelId(),
					newStatus,
					routingKey,
					event.getStatus());
		} catch (ResourceNotFoundException ex) {
			log.warn("Parcel {} not found for status event {}", event.getParcelId(), routingKey);
		}
	}

	private ParcelStatusEvent parseEvent(byte[] body) throws Exception {
		JsonNode root = objectMapper.readTree(body);
		// Older Jackson type wrapper: ["com.example.Type", { ... }]
		if (root.isArray() && root.size() >= 2 && root.get(1).isObject()) {
			root = root.get(1);
		}
		ParcelStatusEvent event = objectMapper.treeToValue(root, ParcelStatusEvent.class);
		if (event == null) {
			throw new IllegalArgumentException("Empty event body");
		}
		return event;
	}

	private ParcelStatus mapStatus(String routingKey, String eventStatus) {
		ParcelStatus fromKey = mapRoutingKey(routingKey);
		if (fromKey != null) {
			return fromKey;
		}
		if (!StringUtils.hasText(eventStatus)) {
			return null;
		}
		return switch (eventStatus.trim().toUpperCase()) {
			case "ASSIGNED" -> ParcelStatus.ASSIGNED;
			case "PICKED_UP", "IN_TRANSIT" -> ParcelStatus.IN_TRANSIT;
			case "DELIVERED", "COMPLETE", "COMPLETED" -> ParcelStatus.DELIVERED;
			default -> null;
		};
	}

	private ParcelStatus mapRoutingKey(String routingKey) {
		if (routingKey == null) {
			return null;
		}
		return switch (routingKey) {
			case "parcel.assigned" -> ParcelStatus.ASSIGNED;
			case "parcel.pickedup" -> ParcelStatus.IN_TRANSIT;
			case "parcel.delivered" -> ParcelStatus.DELIVERED;
			default -> null;
		};
	}
}
