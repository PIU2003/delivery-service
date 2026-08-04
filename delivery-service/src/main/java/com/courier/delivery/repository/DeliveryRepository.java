package com.courier.delivery.repository;

import com.courier.delivery.entity.Delivery;
import com.courier.delivery.entity.DeliveryStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

	List<Delivery> findByParcelIdOrderByAssignedAtDesc(Long parcelId);

	Optional<Delivery> findFirstByParcelIdAndStatusInOrderByAssignedAtDesc(
			Long parcelId, List<DeliveryStatus> statuses);
}
