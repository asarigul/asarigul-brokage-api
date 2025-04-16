package com.brokage.api.exception;

import org.springframework.http.HttpStatus;

public class CustomerAlreadyExistsException extends BrokageApiException  {
	private static final long serialVersionUID = 4727613954430261066L;

	public CustomerAlreadyExistsException(String username) {
        super(String.format("Customer: %s already exists", username));
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
