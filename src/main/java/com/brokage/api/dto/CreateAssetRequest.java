package com.brokage.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateAssetRequest(
	@Positive(message = "Positive customerId required")
	Long customerId, 
	
	@NotBlank(message = "Non-blank assetName required")
	String assetName, 
	
	@Positive(message = "Positive size required")
	BigDecimal size, 
	
	@Positive(message = "Positive usableSize required")
	BigDecimal usableSize
) {}
