package com.brokage.api.config;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.brokage.api.model.Customer;

@Component
public class ApplicationConfig {
	@Value("${jwt.expiryMinutes:180}")
	private Long jwtExpiryMinutes;

	@Value("${jwt.secretKey}")
	private String jwtSecretKey;
	
	@Value("${users:}")
	private List<String> userDefinitions;

	public Long getJwtExpiryMinutes() {
		return jwtExpiryMinutes;
	}

	public String getJwtSecretKey() {
		return jwtSecretKey;
	}
	
	public List<Customer> getPredefinedUsers() {
		if(userDefinitions.isEmpty()) {
			return Collections.emptyList();
		}
		
		return userDefinitions
				.stream()
				.map(s -> {
					String[] parts = s.split(":");
					return new Customer(parts[0], parts[1], Boolean.valueOf(parts[2]));
				}).toList();
	}
}
