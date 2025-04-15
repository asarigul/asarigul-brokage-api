package com.brokage.api.dto;

import com.brokage.api.model.Customer;

public record CreateCustomerResponse(Long id, String username, boolean isAdmin) {

	public static CreateCustomerResponse from(Customer customer) {
		return new CreateCustomerResponse(customer.getId(), customer.getUsername(), customer.isAdmin());
	}
}
