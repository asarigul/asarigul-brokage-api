package com.brokage.api.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.brokage.api.dto.OrderRequest;
import com.brokage.api.dto.OrderResponse;
import com.brokage.api.model.Asset;
import com.brokage.api.model.Customer;
import com.brokage.api.model.Order;
import com.fasterxml.jackson.core.type.TypeReference;

public class CustomerOrderIntegrationTest extends BaseIntegrationTest {

	@Autowired
	private TestHelper helper;
	
	@BeforeAll
	static void setup(@Autowired TestHelper helper) throws Exception {
		helper.ensureAssetBalance("TRY", BigDecimal.valueOf(1000));
	}

	@Test
	void createBuyOrder_shouldSucceed_whenCustomerHasSufficientBalance() throws Exception {
		createOrderAndAssertBalances(Order.Side.BUY, "BTC", BigDecimal.ONE, BigDecimal.TEN);
	}

	@Test
	void createSellOrder_shouldSucceed_whenCustomerHasSufficientBalance() throws Exception {
		createOrderAndAssertBalances(Order.Side.SELL, "ETH", BigDecimal.ONE, BigDecimal.TEN);
	}

	void createOrderAndAssertBalances(Order.Side side, String assetName, BigDecimal orderSize,
			BigDecimal orderPrice) throws Exception {
		Customer customer = helper.customer();

		Asset tryAssetBefore = helper.getAsset("TRY").get();
		Asset otherAssetBefore = helper.createAsset(assetName, BigDecimal.valueOf(100), customer);

		OrderResponse createOrderResponse = createOrder(assetName, side, orderSize, orderPrice);

		assertEquals(Order.Status.PENDING, createOrderResponse.status());
		assertEquals(customer.getId(), createOrderResponse.customerId());
		assertNotNull(createOrderResponse.id());
		assertNotNull(createOrderResponse.createDate());
		assertEquals(assetName, createOrderResponse.assetName());
		assertEquals(orderPrice, createOrderResponse.price());
		assertEquals(orderSize, createOrderResponse.size());
		assertEquals(side, createOrderResponse.side());

		Asset tryAssetAfter = helper.getAsset("TRY").get();
		Asset otherAssetAfter = helper.getAsset(assetName).get();

		BigDecimal totalAmount = orderSize.multiply(orderPrice);

		if (side == Order.Side.BUY) {
			// TRY usable size is deducted, size is unchanged
			assertEquals(0, tryAssetBefore.getSize().compareTo(tryAssetAfter.getSize()));
			assertEquals(0, tryAssetBefore.getUsableSize().compareTo(tryAssetAfter.getUsableSize().add(totalAmount)));

			// other asset size increased, usable unchanged
			assertEquals(0, otherAssetAfter.getSize().compareTo(otherAssetBefore.getSize().add(orderSize)));
			assertEquals(0, otherAssetBefore.getUsableSize().compareTo(otherAssetAfter.getUsableSize()));

		} else if (side == Order.Side.SELL) {
			// TRY size increased, usable unchanged
			assertEquals(0, tryAssetBefore.getUsableSize().compareTo(tryAssetAfter.getUsableSize()));
			assertEquals(0, tryAssetAfter.getSize().compareTo(tryAssetBefore.getSize().add(totalAmount)));

			// other asset size unchanged, usable decreased
			assertEquals(0, otherAssetAfter.getSize().compareTo(otherAssetBefore.getSize()));
			assertEquals(0, otherAssetBefore.getUsableSize().compareTo(otherAssetAfter.getUsableSize().add(orderSize)));
		}
	}

	@Test
	void deleteOrder_shouldSucceed_whenOrderIsPending() throws Exception {
		String assetName = "AAPL";
		OrderResponse createOrderResponse = createOrder(assetName, Order.Side.BUY, BigDecimal.ONE, BigDecimal.TEN);

		Asset tryAssetBefore = helper.getAsset("TRY").get();
		Asset otherAssetBefore = helper.getAsset(assetName).get();

		String responseStr = helper.delete("/api/orders/" + createOrderResponse.id(), helper.token())
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		Asset tryAssetAfter = helper.getAsset("TRY").get();
		Asset otherAssetAfter = helper.getAsset(assetName).get();

		OrderResponse response = objectMapper.readValue(responseStr, OrderResponse.class);

		// assert status changed
		assertEquals(response.status(), Order.Status.CANCELED);

		BigDecimal totalAmount = response.price().multiply(response.size());

		// assert deducted asset (TRY for BUY) size unchanged, usableSize is restored
		assertEquals(tryAssetAfter.getSize().compareTo(tryAssetBefore.getSize()), 0);
		assertEquals(tryAssetAfter.getUsableSize().compareTo(tryAssetBefore.getUsableSize().add(totalAmount)), 0);

		// assert other asset (BTC) size deducted, usableSize unchanged
		assertEquals(otherAssetBefore.getSize().compareTo(otherAssetAfter.getSize().add(response.size())), 0);
		assertEquals(otherAssetAfter.getUsableSize().compareTo(otherAssetBefore.getUsableSize()), 0);
	}

	@Test
	void listOrders_shouldReturnOnlyAndAllOwnerCustomerOrders() throws Exception {
		final int orderCount = 5;

		Set<Long> orderIds = new HashSet<>();
		for (int i = 0; i < orderCount; i++) {
			OrderResponse orderResponse = createOrder("BTC", Order.Side.BUY, BigDecimal.ONE, BigDecimal.TEN);
			orderIds.add(orderResponse.id());
		}

		String response = helper.get("/api/orders", helper.token()).andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

		List<OrderResponse> orders = objectMapper.readValue(response, new TypeReference<List<OrderResponse>>() {
		});

		// assert all orders created for correct customer
		assertTrue(orders.stream().allMatch(o -> o.customerId().equals(helper.customer().getId())));

		// assert all created orders are returned
		assertTrue(orders.stream().map(OrderResponse::id).collect(Collectors.toSet()).containsAll(orderIds));
	}

	private OrderResponse createOrder(String assetName, Order.Side side, BigDecimal size, BigDecimal price)
			throws Exception {
		OrderRequest orderRequest = new OrderRequest(assetName, side, size, price);
		String createResponseStr = helper.post("/api/orders", orderRequest, helper.token())
				.andExpect(status().is(CREATED)).andReturn().getResponse().getContentAsString();

		return objectMapper.readValue(createResponseStr, OrderResponse.class);
	}
}
