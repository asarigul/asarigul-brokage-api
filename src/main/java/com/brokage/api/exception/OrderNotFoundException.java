package com.brokage.api.exception;

import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends BrokageApiException  {
	private static final long serialVersionUID = 4882858803523020253L;

	public OrderNotFoundException(Long orderId) {
        super("Pending Order " + orderId + " not found.");
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
