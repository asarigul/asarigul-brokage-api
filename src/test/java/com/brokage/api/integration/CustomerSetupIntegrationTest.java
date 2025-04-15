package com.brokage.api.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.brokage.api.config.ApplicationConfig;
import com.brokage.api.model.Customer;
import com.brokage.api.repository.CustomerRepository;

public class CustomerSetupIntegrationTest extends BaseIntegrationTest {
	@Autowired
	private ApplicationConfig config;
	
	@Autowired
	private CustomerRepository customerRepository;
	
	@Test
	void testCustomerAutoCreation_succeed() {
		for(Customer next : config.getPredefinedUsers()) {
			assertTrue(customerRepository.findByUsername(next.getUsername()).isPresent());
		}
	}
}
