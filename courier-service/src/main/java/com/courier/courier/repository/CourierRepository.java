package com.courier.courier.repository;

import com.courier.courier.entity.Courier;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CourierRepository extends MongoRepository<Courier, String> {

	List<Courier> findByIsAvailableTrueAndCurrentAreaIgnoreCase(String currentArea);

	List<Courier> findByIsAvailableTrue();
}
