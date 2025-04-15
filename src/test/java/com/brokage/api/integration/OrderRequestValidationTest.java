package com.brokage.api.integration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.brokage.api.dto.OrderRequest;
import com.brokage.api.model.OrderSide;

public class OrderRequestValidationTest extends BaseIntegrationTest {
	private static final String VALID_ASSET_NAME = "USD";
	private static final BigDecimal VALID_SIZE = BigDecimal.ONE;
	private static final BigDecimal VALID_PRICE = BigDecimal.ONE;
	private static final OrderSide VALID_SIDE = OrderSide.SELL;

	@Autowired
	private TestHelper helper;

	@Test
	public void testOrderRequest_MissingAssetName() throws Exception {
		final String missingAssetName = null;
		testInvalidOrderRequest(missingAssetName, VALID_SIDE, VALID_SIZE, VALID_PRICE);
	}
	
	@Test
	public void testOrderRequest_MissingOrderSide() throws Exception {
		final OrderSide missingOrderSide = null;
		testInvalidOrderRequest(VALID_ASSET_NAME, missingOrderSide, VALID_SIZE, VALID_PRICE);
	}
	
	@Test
	public void testOrderRequest_InvalidSize() throws Exception {
		final BigDecimal missingSize = null;
		testInvalidOrderRequest(VALID_ASSET_NAME, VALID_SIDE, missingSize, VALID_PRICE);
		
		final BigDecimal zeroSize = BigDecimal.ZERO;
		testInvalidOrderRequest(VALID_ASSET_NAME, VALID_SIDE, zeroSize, VALID_PRICE);
		
		final BigDecimal negativeSize = BigDecimal.valueOf(-99);
		testInvalidOrderRequest(VALID_ASSET_NAME, VALID_SIDE, negativeSize, VALID_PRICE);
	}
	
	@Test
	public void testOrderRequest_InvalidPrice() throws Exception {
		final BigDecimal missingPrice = null;
		testInvalidOrderRequest(VALID_ASSET_NAME, VALID_SIDE, VALID_SIZE, missingPrice);
		
		final BigDecimal zeroPrice = BigDecimal.ZERO;
		testInvalidOrderRequest(VALID_ASSET_NAME, VALID_SIDE, VALID_SIZE, zeroPrice);
		
		final BigDecimal negativePrice = BigDecimal.valueOf(-99);
		testInvalidOrderRequest(VALID_ASSET_NAME, VALID_SIDE, VALID_SIZE, negativePrice);
	}

	private void testInvalidOrderRequest(String assetName, OrderSide side, BigDecimal size, BigDecimal price)
			throws Exception {
		OrderRequest request = new OrderRequest(assetName, side, size, price);
		
		helper.post("/api/orders", request, helper.token())
			.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
	}
	
}
