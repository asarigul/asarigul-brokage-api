package com.brokage.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCustomerRequest(
	@NotBlank(message = "Non-blank username required")
	String userName, 
	
	@NotBlank(message = "Non-blank password required")
	String password, 
	
	boolean IsAdmin
) {}
