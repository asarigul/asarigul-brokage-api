package com.brokage.api.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.brokage.api.dto.CreateCustomerResponse;
import com.brokage.api.exception.CustomerAlreadyExistsException;
import com.brokage.api.exception.SecurityException;
import com.brokage.api.model.Customer;
import com.brokage.api.repository.CustomerRepository;

@Service
public class CustomerService extends BaseService {
	private final CustomerRepository customerRepository;
	private final PasswordEncoder passwordEncoder;

	public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
		this.customerRepository = customerRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public CreateCustomerResponse createCustomer(String username, String password, boolean isAdmin) {
		if(! isAdmin()) {
			throw SecurityException.adminRequired();
		}
		
		if(customerRepository.findByUsername(username).isPresent()) {
			throw new CustomerAlreadyExistsException(username);
		}
		
		Customer customer = new Customer();
		customer.setUsername(username);
		customer.setPassword(passwordEncoder.encode(password));
		customer.setAdmin(isAdmin);
		customer = customerRepository.save(customer);
		
		logger.info("created customer {} by admin {}", username, getAuthenticatedCustomerId());
		
		return CreateCustomerResponse.from(customer);
	}
	
	
}
