package com.courier.delivery.repository;

import com.courier.delivery.entity.Delivery;
import com.courier.delivery.entity.DeliveryStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeliveryRepository extends MongoRepository<Delivery, String> {

	List<Delivery> findByParcelIdOrderByAssignedAtDesc(String parcelId);

	Optional<Delivery> findFirstByParcelIdAndStatusInOrderByAssignedAtDesc(
			String parcelId, List<DeliveryStatus> statuses);

	List<Delivery> findByStatusInOrderByAssignedAtDesc(List<DeliveryStatus> statuses);
}
