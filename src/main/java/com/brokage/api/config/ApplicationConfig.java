package com.brokage.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationConfig {
	@Value("${jwt.expiryMinutes:180}")
	private Long jwtExpiryMinutes;

	@Value("${jwt.secretKey}")
	private String jwtSecretKey;
	
	@Value("${spring.profiles.active:default}")
    private String activeProfile;

	public Long getJwtExpiryMinutes() {
		return jwtExpiryMinutes;
	}

	public String getJwtSecretKey() {
		return jwtSecretKey;
	}

	public String getActiveProfile() {
		return activeProfile;
	}
}
