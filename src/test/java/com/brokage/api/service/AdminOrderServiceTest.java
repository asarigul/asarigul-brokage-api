package com.brokage.api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.brokage.api.exception.OrderNotFoundException;
import com.brokage.api.exception.SecurityException;
import com.brokage.api.repository.OrderRepository;


public class AdminOrderServiceTest extends BaseTest {
	
	@Mock
	private OrderRepository orderRepository;

	@Mock
	private AuthenticationService authService;;

	@InjectMocks
	private OrderService orderService;

	
	@Test
	void matchOrder_shouldFail_whenUserIsNotAdmin() {
		when(authService.isAdmin()).thenReturn(false);
		
		assertThrows(SecurityException.class, () -> {
			orderService.matchOrder(1L);			
		});
	}
	
	@Test
	void matchOrder_shouldFail_whenOrderNotFound() {
		when(authService.isAdmin()).thenReturn(true);
		
		assertThrows(OrderNotFoundException.class, () -> {
			orderService.matchOrder(999L);			
		});
	}
}
