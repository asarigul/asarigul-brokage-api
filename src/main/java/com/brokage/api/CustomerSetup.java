package com.brokage.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.brokage.api.config.ApplicationConfig;
import com.brokage.api.model.Customer;
import com.brokage.api.repository.CustomerRepository;

import jakarta.annotation.PostConstruct;

@Component
public class CustomerSetup {
	private final ApplicationConfig config;
	private final PasswordEncoder passwordEncoder;
	private final CustomerRepository customerRepository;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public CustomerSetup(ApplicationConfig config, PasswordEncoder passwordEncoder,
			CustomerRepository customerRepository) {
		this.config = config;
		this.passwordEncoder = passwordEncoder;
		this.customerRepository = customerRepository;
	}

	@PostConstruct
	public void createPredefinedUsers() {
		for(Customer next : config.getPredefinedUsers()) {
			next.setPassword(passwordEncoder.encode(next.getPassword()));
			customerRepository.save(next);
		}
		logger.info("predefined users created");
	}
}
