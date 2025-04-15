package com.brokage.api.integration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.brokage.api.dto.LoginRequest;
import com.brokage.api.model.Customer;
import com.fasterxml.jackson.core.JsonProcessingException;

public class AuthenticationIntegrationTest extends BaseIntegrationTest {
	
	@Autowired
	private TestHelper helper;
	
	@Test
	void login_shouldReturnJwtToken_whenCredentialsAreValid() throws Exception {
		Customer customer = helper.customer();
		LoginRequest loginRequest = new LoginRequest(customer.getUsername(), customer.getPassword());
		
		helper.post("/api/login", loginRequest, null)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").exists());
	}

	@Test
	void login_shouldFail_whenCredentialsAreInvalid() throws JsonProcessingException, Exception {
		LoginRequest loginRequest = new LoginRequest("ghost", "ghost");
		
		helper.post("/api/login", loginRequest, null)
			.andExpect(status().is(UNAUTHORIZED));
	}

	@Test
	void accessProtectedEndpoint_shouldSucceedWithValidToken() throws Exception {
		helper.get("/api/orders", helper.token())
		 	.andExpect(status().isOk());
	}

	@Test
	void accessProtectedEndpoint_shouldFailWithInvalidToken() throws Exception {
		helper.post("/api/orders", "", null)
			.andExpect(status().is(UNAUTHORIZED));
	}
}
