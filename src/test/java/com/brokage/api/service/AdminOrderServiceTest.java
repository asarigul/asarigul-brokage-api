package com.brokage.api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.brokage.api.exception.OrderNotFoundException;
import com.brokage.api.exception.SecurityException;
import com.brokage.api.repository.OrderRepository;


@ExtendWith(MockitoExtension.class)
public class AdminOrderServiceTest {
	
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
