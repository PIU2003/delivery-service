package com.courier.courier;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CourierControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createCourierRejectsInvalidPhone() throws Exception {
		mockMvc.perform(post("/api/couriers")
						.header("X-API-KEY", "test-courier-key")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name":"Test Rider",
								  "phone":"bad-phone",
								  "vehicleType":"Bike",
								  "currentArea":"Colombo",
								  "isAvailable":true
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Validation failed"))
				.andExpect(jsonPath("$.fields.phone").exists());
	}

	@Test
	void createThenListByArea() throws Exception {
		mockMvc.perform(post("/api/couriers")
						.header("X-API-KEY", "test-courier-key")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name":"Nimal Jayasuriya",
								  "phone":"+94770001122",
								  "vehicleType":"Van",
								  "currentArea":"Negombo",
								  "isAvailable":false
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.currentArea").value("Negombo"));

		mockMvc.perform(get("/api/couriers/by-area/Negombo").header("X-API-KEY", "test-courier-key"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Nimal Jayasuriya"))
				.andExpect(jsonPath("$[0].isAvailable").value(false));
	}

	@Test
	void missingApiKeyIsRejected() throws Exception {
		mockMvc.perform(get("/api/couriers")).andExpect(status().isUnauthorized());
	}
}
