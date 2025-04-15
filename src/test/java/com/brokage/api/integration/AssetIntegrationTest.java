package com.brokage.api.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.brokage.api.dto.AssetResponse;
import com.fasterxml.jackson.core.type.TypeReference;

public class AssetIntegrationTest extends BaseIntegrationTest {
	@Autowired
	private TestHelper helper;
	
	private static final Set<String> ASSETS = new HashSet<>(Arrays.asList("A", "B", "C", "D"));
	
	@BeforeAll
	static void setup(@Autowired TestHelper helper) throws Exception {
		for(String assetName : ASSETS) {
			helper.createAsset(assetName, BigDecimal.TEN, helper.customer());
			helper.createAsset(assetName, BigDecimal.ONE, helper.admin());
		}
	}
	
	@Test
	void listAssets_shouldReturnCustomerAssets_forCustomer() throws Exception {
		String token = helper.token();
		String listResponseStr = helper.get("/api/assets", token).andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		
		List<AssetResponse> assets = objectMapper.readValue(listResponseStr, new TypeReference<List<AssetResponse>>() {});
	
		assertTrue(assets.stream().map(AssetResponse::customerId).allMatch(x -> x.equals(helper.customer().getId())));
		Set<String> assetNames = assets.stream().map(AssetResponse::assetName).collect(Collectors.toSet());
		assertTrue(ASSETS.stream().allMatch(a -> assetNames.contains(a)));
	}
	
	@Test
	void listAssets_shouldFail_IfNotOwner() throws Exception {
		// customer tries to list admin's assets
		String token = helper.token();
		helper.get("/api/assets/" + helper.admin().getId(), token).andExpect(status().is(UNAUTHORIZED));
	}
	
	@Test
	void listAssets_shouldReturnCustomerAssets_forAdmin() throws Exception {
		// admin tries to list customer's assets
		String token = helper.adminToken();
		helper.get("/api/assets/" + helper.customer().getId(), token).andExpect(status().isOk());
	}
}
