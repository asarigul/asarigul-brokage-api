package com.brokage.api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.brokage.api.exception.SecurityException;

@ExtendWith(MockitoExtension.class)
public class CreateCustomerServiceTest {
	
	@Spy
	@InjectMocks
	private CustomerService customerService;
	
	@Test
	void createCustomer_shouldFail_whenCallerIsNotAdmin() {
		doReturn(false).when(customerService).isAdmin();
		
		assertThrows(SecurityException.class, () -> {
			customerService.createCustomer("customer", "password", false);
		});
	}

}
