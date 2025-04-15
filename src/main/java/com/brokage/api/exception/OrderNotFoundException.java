package com.brokage.api.exception;

import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends BrokageApiException  {
	private static final long serialVersionUID = 4882858803523020253L;

	public OrderNotFoundException(Long orderId) {
        super(String.format("Pending order: %d not found", orderId));
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
