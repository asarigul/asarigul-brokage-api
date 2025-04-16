package com.brokage.api.exception;

import org.springframework.http.HttpStatus;

public class InvalidArgumentException extends BrokageApiException  {
	private static final long serialVersionUID = -2050199266453040295L;

	public InvalidArgumentException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
