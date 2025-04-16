package com.brokage.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCustomerRequest(
	@NotBlank(message = "Non-blank username required")
	String username, 
	
	@NotBlank(message = "Non-blank password required")
	String password, 
	
	@NotNull(message = "isAdmin boolean value required")
	Boolean isAdmin
) {}
