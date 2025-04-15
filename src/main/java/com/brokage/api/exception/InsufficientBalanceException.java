package com.brokage.api.exception;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends BrokageApiException {
	private static final long serialVersionUID = -6759489190109111167L;

	public InsufficientBalanceException(Long customerId, BigDecimal amount, String assetName) {
		super("Customer " + customerId + " does not have " + amount.toString() + " " + assetName);
	}

	@Override
	public HttpStatus getStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
