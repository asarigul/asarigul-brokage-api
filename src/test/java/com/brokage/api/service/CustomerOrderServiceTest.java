package com.brokage.api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.brokage.api.exception.InsufficientBalanceException;
import com.brokage.api.exception.OrderNotFoundException;
import com.brokage.api.exception.SecurityException;
import com.brokage.api.model.Asset;
import com.brokage.api.model.Order;
import com.brokage.api.model.Order.Side;
import com.brokage.api.repository.AssetRepository;
import com.brokage.api.repository.OrderRepository;

public class CustomerOrderServiceTest extends BaseTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private AssetRepository assetRepository;

	@Mock
	private AuthenticationService authService;;

	@InjectMocks
	private OrderService orderService;

	private static final Long CUSTOMER_ID = 1L;
	
	@BeforeEach
	void mockAuthentication() {
		when(authService.getAuthenticatedCustomerId()).thenReturn(CUSTOMER_ID);
	}


	@Test
	void createOrder_shouldFail_whenInsufficientBalance() {
		final BigDecimal insufficientTRY = BigDecimal.ONE;
		String otherAsset = "BTC";
		Side orderSide = Order.Side.BUY;
		BigDecimal orderSize = BigDecimal.TEN;
		BigDecimal orderPrice = BigDecimal.TEN;

		mockTRYAsset(insufficientTRY);

		assertThrows(InsufficientBalanceException.class, () -> {
			orderService.createOrder(orderSide, otherAsset, orderSize, orderPrice);
		});
	}
	

	@Test
	void deleteOrder_shouldFail_whenOrderNotOwnedByCustomer() {
		String assetName = "BTC";
		Long orderId = 1L;
		
		// Authenticated customerId (1) != ownerId 
		Long ownerId = 999L;
		mockPendingOrderFind(orderId, ownerId, assetName);

		assertThrows(SecurityException.class, () -> {
			orderService.deleteOrder(orderId);
		});
	}

	@Test
	void deleteOrder_shouldFail_whenNoPendingOrder() {
		// no order for id & PENDING status
		assertThrows(OrderNotFoundException.class, () -> {
			orderService.deleteOrder(1L);			
		});
	}

	private void mockTRYAsset(BigDecimal size) {
		Asset tryAsset = new Asset();
		tryAsset.setAssetName("TRY");
		tryAsset.setCustomerId(CUSTOMER_ID);
		tryAsset.setSize(size);
		tryAsset.setUsableSize(size);

		when(assetRepository.findByCustomerIdAndAssetNameWithLock(CUSTOMER_ID, "TRY"))
				.thenReturn(Optional.of(tryAsset));
	}
	

	private void mockPendingOrderFind(Long id, Long returnedCustomerId, String assetName) {
		Order savedOrder = new Order();
		savedOrder.setId(id);
		savedOrder.setCustomerId(returnedCustomerId);
		savedOrder.setAssetName(assetName);
		savedOrder.setSize(BigDecimal.ONE);
		savedOrder.setPrice(BigDecimal.TEN);
		when(orderRepository.findByIdAndStatus(id, Order.Status.PENDING)).thenReturn(Optional.of(savedOrder));
	}
}
