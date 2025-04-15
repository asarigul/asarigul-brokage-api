package com.brokage.api.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.brokage.api.dto.OrderRequest;
import com.brokage.api.dto.OrderResponse;
import com.brokage.api.model.Asset;
import com.brokage.api.model.Order;

public class SystemConcurrencyTest extends BaseIntegrationTest {
	@Autowired
	private TestHelper helper;
	
	@Test
	void concurrentOrderCreation_shouldBeSafe() throws Exception {
	    String assetName = "SWI";
	    int threadCount = 20;
	    BigDecimal orderSize = BigDecimal.ONE;
	    BigDecimal price = BigDecimal.TEN;
	    
	    final int expectedSuccess = 5;
	    BigDecimal tryBalance = orderSize.multiply(price).multiply(BigDecimal.valueOf(expectedSuccess));
	    
	    helper.ensureAssetBalance("TRY", tryBalance);

	    // Prepare threads
	    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
	    CountDownLatch latch = new CountDownLatch(threadCount);
	    List<Future<OrderResponse>> futures = new ArrayList<>();

	    for (int i = 0; i < threadCount; i++) {
	        futures.add(executor.submit(() -> {
	            try {
	            	// start all together
	                latch.countDown();
	                latch.await();

	                return createOrder(assetName, Order.Side.BUY, orderSize, price);
	            } catch (Exception e) {
	                e.printStackTrace();
	                return null;
	            }
	        }));
	    }

	    executor.shutdown();
	    executor.awaitTermination(threadCount, TimeUnit.SECONDS);

	    // Collect successful orders
	    List<OrderResponse> successfulOrders = new ArrayList<>();
	    for (Future<OrderResponse> future : futures) {
	        try {
	            OrderResponse response = future.get();
	            if (response != null) successfulOrders.add(response);
	        } catch (Exception ignored) {}
	    }

	    assertEquals(expectedSuccess, successfulOrders.size());

	    Asset tryAsset = helper.getAsset("TRY").get();

	    // assert TRY balance
	    assertEquals(0, tryAsset.getSize().compareTo(tryBalance));
	    assertEquals(0, tryAsset.getUsableSize().compareTo(BigDecimal.ZERO));
	    
	    // Asset size should be 5 
	    Asset otherAsset = helper.getAsset(assetName).get();
	    assertEquals(0, otherAsset.getSize().compareTo(BigDecimal.valueOf(expectedSuccess).multiply(orderSize)));
	    assertEquals(0, otherAsset.getUsableSize().compareTo(BigDecimal.ZERO));
	}

	private OrderResponse createOrder(String assetName, Order.Side side, BigDecimal size, BigDecimal price)
			throws Exception {
		OrderRequest orderRequest = new OrderRequest(assetName, side, size, price);
		String createResponseStr = helper.post("/api/orders", orderRequest, helper.token())
				.andExpect(status().is(CREATED)).andReturn().getResponse().getContentAsString();

		return objectMapper.readValue(createResponseStr, OrderResponse.class);
	}
}
