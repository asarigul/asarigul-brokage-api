package com.brokage.api.dto;

import java.math.BigDecimal;

import com.brokage.api.model.Order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderRequest(
		@NotBlank(message = "Non-blank assetName required")
		String assetName,
		
		@NotNull(message = "side required")
		Order.Side side,
		
		@NotNull(message = "Positive size required")
		@Positive(message = "Positive size required")
		BigDecimal size,
		
		@NotNull(message = "Positive price required")
		@Positive(message = "Positive price required")
		BigDecimal price
) {}
