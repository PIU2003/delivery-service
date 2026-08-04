package com.courier.delivery.client;

import com.courier.delivery.dto.CourierDto;
import java.util.List;

public interface CourierServiceClient {

	List<CourierDto> findAvailable(String area);
}
