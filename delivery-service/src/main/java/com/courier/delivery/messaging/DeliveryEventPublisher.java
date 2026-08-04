package com.courier.delivery.messaging;

import com.courier.delivery.entity.Delivery;

public interface DeliveryEventPublisher {

	void publishAssigned(Delivery delivery);

	void publishPickedUp(Delivery delivery);

	void publishDelivered(Delivery delivery);
}
