package com.brokage.api.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.brokage.api.dto.CreateCustomerRequest;
import com.brokage.api.dto.CreateCustomerResponse;

public class CreateCustomerIntegrationTest extends BaseIntegrationTest {
	@Autowired
	private TestHelper helper;
	
	@Test
	void createCustomer_shouldFail_forExistingUsername() throws Exception {
		CreateCustomerRequest request = new CreateCustomerRequest(helper.admin().getUsername(), "password", false);
		helper.post("/api/customers", request, helper.adminToken())
				.andExpect(status().is(BAD_REQUEST));
	}
	
	@Test
	void createCustomer_shouldSucceed_forValidCustomer() throws Exception {
		String username = getClass().getSimpleName();
		boolean isAdmin = false;
		
		CreateCustomerRequest request = new CreateCustomerRequest(username, "password", isAdmin);
		String responseStr = helper.post("/api/customers", request, helper.adminToken())
				.andExpect(status().is(CREATED)).andReturn().getResponse().getContentAsString();
	
		CreateCustomerResponse response = objectMapper.readValue(responseStr, CreateCustomerResponse.class);

		assertNotNull(response.id());
		assertEquals(response.username(), username);
		assertEquals(response.isAdmin(), isAdmin);
	}
}
