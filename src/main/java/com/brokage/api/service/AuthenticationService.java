package com.brokage.api.service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.brokage.api.config.ApplicationConfig;
import com.brokage.api.dto.LoginResponse;
import com.brokage.api.exception.SecurityException;
import com.brokage.api.model.Customer;
import com.brokage.api.repository.CustomerRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class AuthenticationService extends BaseService {
	private final CustomerRepository customerRepository;
	private final ApplicationConfig config;
	private final PasswordEncoder passwordEncoder;

	public AuthenticationService(ApplicationConfig config, CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
		this.config = config;
		this.customerRepository = customerRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	
	public LoginResponse login(String username, String password) {
		Customer customer = customerRepository.findByUsername(username)
				.orElseThrow(() -> new SecurityException("Invalid credentials"));
		
		if(! passwordEncoder.matches(password, customer.getPassword())) {
			throw new SecurityException("Invalid credentials");
		}
		
		String token = generateToken(customer.getId(), customer.isAdmin());
		
		logger.info("Customer {} authenticated", username);
		
		return new LoginResponse(token);
	}
	
	private String generateToken(Long customerId, boolean isAdmin) {
        Key key = Keys.hmacShaKeyFor(config.getJwtSecretKey().getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .setSubject(String.valueOf(customerId))
                .claim("isAdmin", isAdmin)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(config.getJwtExpiryMinutes(), TimeUnit.MINUTES) ))  
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
}
