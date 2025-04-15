package com.brokage.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = "Non-blank username required")
		String username,
		
		@NotBlank(message = "Non-blank password required")
		String password
) {}
