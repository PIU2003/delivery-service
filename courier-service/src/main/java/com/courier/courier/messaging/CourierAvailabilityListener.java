package com.courier.courier.messaging;

import com.courier.courier.dto.ParcelStatusEvent;
import com.courier.courier.exception.ResourceNotFoundException;
import com.courier.courier.service.CourierService;
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
public class CourierAvailabilityListener {

	private final CourierService courierService;
	private final ObjectMapper objectMapper;

	@RabbitListener(queues = "${app.rabbitmq.queue}")
	public void onAvailabilityEvent(
			Message message, @Header(name = AmqpHeaders.RECEIVED_ROUTING_KEY, required = false) String routingKey) {
		ParcelStatusEvent event;
		try {
			event = parseEvent(message.getBody());
		} catch (Exception ex) {
			log.warn("Failed to parse availability event (routingKey={}): {}", routingKey, ex.getMessage());
			return;
		}

		if (!StringUtils.hasText(event.getCourierId())) {
			log.warn("Ignoring availability event with missing courierId (routingKey={})", routingKey);
			return;
		}

		Boolean available = mapAvailability(routingKey, event.getStatus());
		if (available == null) {
			// e.g. pickup events — courier stays busy
			return;
		}

		try {
			courierService.applyAvailabilityFromEvent(event.getCourierId().trim(), available);
			log.info("Courier {} availability set to {} via {}", event.getCourierId(), available, routingKey);
		} catch (ResourceNotFoundException ex) {
			log.warn("Courier {} not found for availability event {}", event.getCourierId(), routingKey);
		}
	}

	private ParcelStatusEvent parseEvent(byte[] body) throws Exception {
		JsonNode root = objectMapper.readTree(body);
		if (root.isArray() && root.size() >= 2 && root.get(1).isObject()) {
			root = root.get(1);
		}
		ParcelStatusEvent event = objectMapper.treeToValue(root, ParcelStatusEvent.class);
		if (event == null) {
			throw new IllegalArgumentException("Empty event body");
		}
		return event;
	}

	private Boolean mapAvailability(String routingKey, String eventStatus) {
		if (routingKey != null) {
			Boolean fromKey = switch (routingKey) {
				case "parcel.assigned" -> false;
				case "parcel.delivered" -> true;
				default -> null;
			};
			if (fromKey != null) {
				return fromKey;
			}
		}
		if (!StringUtils.hasText(eventStatus)) {
			return null;
		}
		return switch (eventStatus.trim().toUpperCase()) {
			case "ASSIGNED", "PICKED_UP", "IN_TRANSIT" -> false;
			case "DELIVERED", "COMPLETE", "COMPLETED" -> true;
			default -> null;
		};
	}
}
