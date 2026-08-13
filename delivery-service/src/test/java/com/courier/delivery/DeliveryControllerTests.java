package com.courier.delivery;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.courier.delivery.config.TestBeansConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestBeansConfig.class)
class DeliveryControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void assignRejectsBlankParcelId() throws Exception {
		mockMvc.perform(post("/api/deliveries/assign")
						.header("X-API-KEY", "test-delivery-key")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"parcelId":"   ","area":"Colombo"}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void assignThenActiveContainsRun() throws Exception {
		mockMvc.perform(post("/api/deliveries/assign")
						.header("X-API-KEY", "test-delivery-key")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"parcelId":"parcel-active-1","area":"Colombo"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("ASSIGNED"))
				.andExpect(jsonPath("$.parcelId").value("parcel-active-1"));

		mockMvc.perform(get("/api/deliveries/active").header("X-API-KEY", "test-delivery-key"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.parcelId=='parcel-active-1')]").exists());
	}

	@Test
	void missingApiKeyIsRejected() throws Exception {
		mockMvc.perform(get("/api/deliveries/active")).andExpect(status().isUnauthorized());
	}
}
