package com.brokage.api.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.brokage.api.dto.AssetResponse;
import com.brokage.api.dto.CreateAssetRequest;
import com.brokage.api.dto.CreateCustomerRequest;
import com.brokage.api.dto.CreateCustomerResponse;
import com.brokage.api.model.Asset;

public class CreateAssetIntegrationTest extends BaseIntegrationTest {
	@Autowired
	private TestHelper helper;
	
	@Test
	void createAsset_shouldFail_forUnExistingCustomer() throws Exception {
		CreateAssetRequest request = new CreateAssetRequest(999L, "USD", BigDecimal.ONE, BigDecimal.ONE);
		
		helper.post("/api/assets", request, helper.adminToken())
				.andExpect(status().is(NOT_FOUND));
	}
	
	@Test
	void createAsset_shouldFail_whenCustomerAlreadyHasAsset() throws Exception {
		Asset tryAsset = helper.tryAsset(); // TRY asset created on test startup
		
		CreateAssetRequest request = new CreateAssetRequest(tryAsset.getCustomerId(), tryAsset.getAssetName(), BigDecimal.ONE, BigDecimal.ONE);
		
		helper.post("/api/assets", request, helper.adminToken())
				.andExpect(status().is(BAD_REQUEST));
	}
	
	@Test
	void createAsset_shouldSucceed_whenValidRequest() throws Exception {
		Long customerId = helper.customer().getId();
		String assetName = UUID.randomUUID().toString().substring(0, 4);
		
		BigDecimal size = BigDecimal.TEN;
		BigDecimal usableSize = BigDecimal.ONE;
		
		CreateAssetRequest request = new CreateAssetRequest(customerId, assetName, size, usableSize);
		
		String responseStr = helper.post("/api/assets", request, helper.adminToken())
				.andExpect(status().is(CREATED)).andReturn().getResponse().getContentAsString();
		
		AssetResponse response = objectMapper.readValue(responseStr, AssetResponse.class);
		
		assertNotNull(response.id());
		assertEquals(response.customerId(), customerId);
		assertEquals(response.assetName(), assetName);
		assertEquals(0, response.size().compareTo(size));
		assertEquals(0, response.usableSize().compareTo(usableSize));
	}
}
