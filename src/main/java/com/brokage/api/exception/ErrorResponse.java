package com.brokage.api.exception;

import java.time.LocalDateTime;

public record ErrorResponse(LocalDateTime timestamp, String message) {
	static ErrorResponse from(String message) {
		return new ErrorResponse(LocalDateTime.now(), message);
	}
}
