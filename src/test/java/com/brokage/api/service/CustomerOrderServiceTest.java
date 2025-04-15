package com.brokage.api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.brokage.api.exception.AssetNotFoundException;
import com.brokage.api.exception.InsufficientBalanceException;
import com.brokage.api.exception.OrderNotFoundException;
import com.brokage.api.exception.SecurityException;
import com.brokage.api.model.Asset;
import com.brokage.api.model.Customer;
import com.brokage.api.model.Order;
import com.brokage.api.model.OrderSide;
import com.brokage.api.model.OrderStatus;
import com.brokage.api.repository.AssetRepository;
import com.brokage.api.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
public class CustomerOrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private AssetRepository assetRepository;

	@Spy
	@InjectMocks
	private OrderService orderService;

	private static final Long CUSTOMER_ID = 1L;
	
	@BeforeEach
	void mockAuthentication() {
		doReturn(CUSTOMER_ID).when(orderService).getAuthenticatedCustomerId();
	}


	@Test
	void createOrder_shouldFail_whenInsufficientBalance() {
		final BigDecimal insufficientTRY = BigDecimal.ONE;
		mockTRYAsset(insufficientTRY);

		assertThrows(InsufficientBalanceException.class, () -> {
			orderService.createOrder(OrderSide.BUY, "BTC", BigDecimal.TEN, BigDecimal.TEN);
		});
	}
	
	@Test
	void createSellOrder_shouldFail_whenMissingAsset() {
		mockTRYAsset(BigDecimal.TEN);

		assertThrows(AssetNotFoundException.class, () -> {
			orderService.createOrder(OrderSide.SELL, "BTC", BigDecimal.TEN, BigDecimal.TEN);
		});
	}
	
	private void mockTRYAsset(BigDecimal size) {
		Asset tryAsset = new Asset();
		tryAsset.setAssetName("TRY");
		tryAsset.setCustomer(new Customer(CUSTOMER_ID));
		tryAsset.setSize(size);
		tryAsset.setUsableSize(size);

		when(assetRepository.findByCustomerIdAndAssetNameWithLock(CUSTOMER_ID, "TRY"))
				.thenReturn(Optional.of(tryAsset));
	}

	@Test
	void deleteOrder_shouldFail_whenOrderNotOwnedByCustomer() {
		String assetName = "BTC";
		Long orderId = 1L;
		
		// Authenticated customerId (1) != ownerId 
		Long ownerId = 999L;

		Order savedOrder = new Order();
		savedOrder.setId(orderId);
		savedOrder.setCustomer(new Customer(ownerId));
		savedOrder.setAssetName(assetName);
		savedOrder.setSize(BigDecimal.ONE);
		savedOrder.setPrice(BigDecimal.TEN);
		
		when(orderRepository.findByIdAndStatus(orderId, OrderStatus.PENDING)).thenReturn(Optional.of(savedOrder));

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

}
