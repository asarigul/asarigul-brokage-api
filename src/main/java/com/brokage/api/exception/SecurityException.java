package com.brokage.api.exception;

import org.springframework.http.HttpStatus;

public class SecurityException extends BrokageApiException  {
	private static final long serialVersionUID = 4882858803523020253L;

	public SecurityException(String message) {
        super(message);
    }
	
	public static SecurityException adminRequired() {
		return new SecurityException("Admin privileges required");
	}

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}
