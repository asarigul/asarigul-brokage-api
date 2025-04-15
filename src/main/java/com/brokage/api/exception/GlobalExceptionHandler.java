package com.brokage.api.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public record ErrorResponse(LocalDateTime timestamp, String message) {
		static ErrorResponse from(String message) {
			return new ErrorResponse(LocalDateTime.now(), message);
		}
	}

	@ExceptionHandler(BrokageApiException.class)
	public ResponseEntity<ErrorResponse> handleBrokageException(BrokageApiException ex) {
		logger.error("API exception", ex);
		return ResponseEntity.status(ex.getStatus()).body(ErrorResponse.from(ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
		logger.error("Unhandled exception", ex);
		
		Throwable cause = ex.getCause();
		if(cause != null && cause instanceof BrokageApiException) {
			return handleBrokageException((BrokageApiException) ex.getCause());
		}
		
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.from("Something went wrong."));
	}
	
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
			WebRequest request) {
		logger.error("Validation exception", ex);
		
		List<String> errorMessages = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.toList());

		return badRequestResponseWithMessage("Invalid parameter: " + errorMessages);
	}
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
		return badRequestResponseWithMessage("Missing or malformed request body");
	}
	
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleWrongMethod(HttpRequestMethodNotSupportedException ex) {
		return badRequestResponseWithMessage(ex.getMessage());
	}

	
	private ResponseEntity<ErrorResponse> badRequestResponseWithMessage(String message) {
		ErrorResponse response = ErrorResponse.from(message);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}
}