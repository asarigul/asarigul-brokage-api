package com.brokage.api.exception;

import org.springframework.http.HttpStatus;

public abstract class BrokageApiException extends RuntimeException {
	private static final long serialVersionUID = 7408898364174192559L;

	public BrokageApiException(String message) {
        super(message);
    }

    public BrokageApiException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public abstract HttpStatus getStatus();
}
