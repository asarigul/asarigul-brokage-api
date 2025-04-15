package com.brokage.api.service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
public class AuthenticationService {
	private final CustomerRepository customerRepository;
	private final ApplicationConfig config;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public AuthenticationService(ApplicationConfig config, CustomerRepository customerRepository) {
		this.config = config;
		this.customerRepository = customerRepository;
	}
	
	
	public LoginResponse login(String username, String password) {
		Customer customer = customerRepository.findByUsernameAndPassword(username, password)
				.orElseThrow(() -> new SecurityException("Invalid credentials"));
		
		String token = generateToken(customer.getId(), customer.isAdmin());
		
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
	
	public Long getAuthenticatedCustomerId() {
		return Long.valueOf(ensureAuthentication().getPrincipal().toString());
	}

	public boolean isAdmin() {
		return (Boolean) ensureAuthentication().getCredentials();
	}
	
	public void createAdminUser() {
		if(customerRepository.findByIsAdmin(true).isEmpty()) {
			Customer admin = new Customer();
			admin.setAdmin(true);
			admin.setUsername("admin");
			admin.setPassword("admin");
			customerRepository.save(admin);
			
			logger.warn("Default admin user created. Username: " + admin.getUsername() + ", password: " + admin.getPassword());
		}
	}
	
	private static Authentication ensureAuthentication() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null) {
			throw new SecurityException("Not authenticated!");
		}
		return authentication;
	}
}
