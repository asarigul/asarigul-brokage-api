package com.brokage.api.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.brokage.api.dto.OrderRequest;
import com.brokage.api.dto.OrderResponse;
import com.brokage.api.model.Asset;
import com.brokage.api.model.Order;

public class AdminOrderIntegrationTest extends BaseIntegrationTest {

	@Autowired
	private TestHelper helper;

	@Test
	void matchOrder_shouldUpdateAssetsForCustomer_onBuy() throws Exception {
		createAndMatchOrderAndAssertBalances(Order.Side.BUY, "EUR", BigDecimal.ONE, BigDecimal.TEN);
	}

	@Test
	void matchOrder_shouldUpdateAssetsForCustomer_onSell() throws Exception {
		createAndMatchOrderAndAssertBalances(Order.Side.SELL, "GBP", BigDecimal.ONE, BigDecimal.TEN);
	}

	void createAndMatchOrderAndAssertBalances(Order.Side side, String assetName, BigDecimal orderSize,
			BigDecimal orderPrice) throws Exception {		
		Asset otherAssetBefore = helper.createAsset(assetName, BigDecimal.valueOf(100), helper.customer());
		Asset tryAssetBefore = helper.getAsset("TRY").get();
		
		OrderResponse createResponse = createOrder(assetName, side, orderSize, orderPrice);

		String matchResponseStr = helper.put("/api/orders/" + createResponse.id(), null, helper.adminToken())
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		OrderResponse matchResponse = objectMapper.readValue(matchResponseStr, OrderResponse.class);

		Asset tryAssetAfter = helper.getAsset("TRY").get();
		Asset otherAssetAfter = helper.getAsset(assetName).get();


		assertEquals(matchResponse.status(), Order.Status.MATCHED);
		
		BigDecimal totalAmount = orderSize.multiply(orderPrice);

		if (side == Order.Side.BUY) {
			// TRY size and usableSize decreased by totalAmount
			assertEquals(0, tryAssetAfter.getUsableSize().compareTo(tryAssetBefore.getUsableSize().subtract(totalAmount)));
			assertEquals(0, tryAssetAfter.getSize().compareTo(tryAssetBefore.getSize().subtract(totalAmount)));

			// Other size and usableSize increased by orderSize
			assertEquals(0, otherAssetAfter.getSize().compareTo(otherAssetBefore.getSize().add(orderSize)));
			assertEquals(0, otherAssetAfter.getUsableSize().compareTo(otherAssetBefore.getUsableSize().add(orderSize)));

		} else if (side == Order.Side.SELL) {
			// TRY size and usableSize increased by totalAmount
			assertEquals(0, tryAssetAfter.getSize().compareTo(tryAssetBefore.getSize().add(totalAmount)));
			assertEquals(0, tryAssetAfter.getUsableSize().compareTo(tryAssetBefore.getUsableSize().add(totalAmount)));

			// Other size and usableSize decreased by orderSize
			assertEquals(0, otherAssetAfter.getSize().compareTo(otherAssetBefore.getSize().subtract(orderSize)));
			assertEquals(0, otherAssetAfter.getUsableSize().compareTo(otherAssetBefore.getUsableSize().subtract(orderSize)));
		}
	}

	private OrderResponse createOrder(String assetName, Order.Side side, BigDecimal size, BigDecimal price)
			throws Exception {
		OrderRequest orderRequest = new OrderRequest(assetName, side, size, price);
		String createResponseStr = helper.post("/api/orders", orderRequest, helper.token())
				.andExpect(status().is(CREATED)).andReturn().getResponse().getContentAsString();

		return objectMapper.readValue(createResponseStr, OrderResponse.class);
	}
}
