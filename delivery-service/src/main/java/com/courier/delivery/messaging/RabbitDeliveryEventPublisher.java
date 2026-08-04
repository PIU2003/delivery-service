package com.courier.delivery.messaging;

import com.courier.delivery.dto.ParcelStatusEvent;
import com.courier.delivery.entity.Delivery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class RabbitDeliveryEventPublisher implements DeliveryEventPublisher {

	private final RabbitTemplate rabbitTemplate;

	@Value("${app.rabbitmq.exchange}")
	private String exchange;

	@Value("${app.rabbitmq.routing-keys.assigned}")
	private String assignedKey;

	@Value("${app.rabbitmq.routing-keys.pickedup}")
	private String pickedUpKey;

	@Value("${app.rabbitmq.routing-keys.delivered}")
	private String deliveredKey;

	@Override
	public void publishAssigned(Delivery delivery) {
		publish(assignedKey, delivery);
	}

	@Override
	public void publishPickedUp(Delivery delivery) {
		publish(pickedUpKey, delivery);
	}

	@Override
	public void publishDelivered(Delivery delivery) {
		publish(deliveredKey, delivery);
	}

	private void publish(String routingKey, Delivery delivery) {
		ParcelStatusEvent event = ParcelStatusEvent.builder()
				.parcelId(delivery.getParcelId())
				.courierId(delivery.getCourierId())
				.deliveryId(delivery.getId())
				.build();
		rabbitTemplate.convertAndSend(exchange, routingKey, event);
		log.info(
				"Published {} for deliveryId={} parcelId={} courierId={}",
				routingKey,
				delivery.getId(),
				delivery.getParcelId(),
				delivery.getCourierId());
	}
}
