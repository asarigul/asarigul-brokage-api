package com.brokage.api.exception;

import org.springframework.http.HttpStatus;

public class CustomerNotFoundException extends BrokageApiException  {
	private static final long serialVersionUID = 4727613954430261066L;

	public CustomerNotFoundException(Long customerId) {
        super(String.format("Customer: %d not found", customerId));
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
