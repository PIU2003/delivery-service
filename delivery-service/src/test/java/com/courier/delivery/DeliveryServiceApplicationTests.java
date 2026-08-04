package com.courier.delivery;

import com.courier.delivery.config.TestBeansConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestBeansConfig.class)
class DeliveryServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}
