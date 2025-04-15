package com.brokage.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brokage.api.dto.CreateCustomerRequest;
import com.brokage.api.dto.CreateCustomerResponse;
import com.brokage.api.service.CustomerService;

import jakarta.validation.Valid;

@RestController("/api/customers")
public class CustomerController {
	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	@PostMapping
	public ResponseEntity<CreateCustomerResponse> createCustomer(@Valid CreateCustomerRequest request) {
		CreateCustomerResponse customer = customerService.createCustomer(request.userName(), request.password(), request.IsAdmin());
		return ResponseEntity.status(HttpStatus.CREATED).body(customer);
	}
}
