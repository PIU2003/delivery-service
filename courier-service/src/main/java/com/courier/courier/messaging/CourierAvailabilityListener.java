package com.courier.courier.messaging;

import com.courier.courier.dto.ParcelStatusEvent;
import com.courier.courier.exception.ResourceNotFoundException;
import com.courier.courier.service.CourierService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

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
			event = objectMapper.readValue(message.getBody(), ParcelStatusEvent.class);
		} catch (Exception ex) {
			log.warn("Failed to parse availability event (routingKey={}): {}", routingKey, ex.getMessage());
			return;
		}

		if (event.getCourierId() == null) {
			log.warn("Ignoring availability event with missing courierId (routingKey={})", routingKey);
			return;
		}

		Boolean available = mapRoutingKey(routingKey);
		if (available == null) {
			log.warn("Ignoring unknown routing key {} for courier {}", routingKey, event.getCourierId());
			return;
		}

		try {
			courierService.applyAvailabilityFromEvent(event.getCourierId(), available);
			log.info("Courier {} availability set to {} via {}", event.getCourierId(), available, routingKey);
		} catch (ResourceNotFoundException ex) {
			log.warn("Courier {} not found for availability event {}", event.getCourierId(), routingKey);
		}
	}

	private Boolean mapRoutingKey(String routingKey) {
		if (routingKey == null) {
			return null;
		}
		return switch (routingKey) {
			case "parcel.assigned" -> false;
			case "parcel.delivered" -> true;
			default -> null;
		};
	}
}
