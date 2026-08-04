package com.courier.parcel.messaging;

import com.courier.parcel.dto.ParcelStatusEvent;
import com.courier.parcel.entity.ParcelStatus;
import com.courier.parcel.exception.ResourceNotFoundException;
import com.courier.parcel.service.ParcelService;
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
public class ParcelStatusListener {

	private final ParcelService parcelService;
	private final ObjectMapper objectMapper;

	@RabbitListener(queues = "${app.rabbitmq.queue}")
	public void onStatusEvent(
			Message message, @Header(name = AmqpHeaders.RECEIVED_ROUTING_KEY, required = false) String routingKey) {
		ParcelStatusEvent event;
		try {
			event = objectMapper.readValue(message.getBody(), ParcelStatusEvent.class);
		} catch (Exception ex) {
			log.warn("Failed to parse parcel status event (routingKey={}): {}", routingKey, ex.getMessage());
			return;
		}

		if (event.getParcelId() == null) {
			log.warn("Ignoring parcel status event with missing parcelId (routingKey={})", routingKey);
			return;
		}

		ParcelStatus newStatus = mapRoutingKey(routingKey);
		if (newStatus == null) {
			log.warn("Ignoring unknown routing key {} for parcel {}", routingKey, event.getParcelId());
			return;
		}

		try {
			parcelService.applyStatusFromEvent(event.getParcelId(), newStatus);
			log.info("Parcel {} status updated to {} via {}", event.getParcelId(), newStatus, routingKey);
		} catch (ResourceNotFoundException ex) {
			log.warn("Parcel {} not found for status event {}", event.getParcelId(), routingKey);
		}
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
