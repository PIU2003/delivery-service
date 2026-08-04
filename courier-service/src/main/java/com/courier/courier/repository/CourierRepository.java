package com.courier.courier.repository;

import com.courier.courier.entity.Courier;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierRepository extends JpaRepository<Courier, Long> {

	List<Courier> findByIsAvailableTrueAndCurrentAreaIgnoreCase(String currentArea);

	List<Courier> findByIsAvailableTrue();
}
