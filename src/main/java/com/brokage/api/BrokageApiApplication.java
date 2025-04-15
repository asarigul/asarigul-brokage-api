package com.brokage.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.brokage.api.config.ApplicationConfig;
import com.brokage.api.service.AuthenticationService;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableAspectJAutoProxy
public class BrokageApiApplication {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final ApplicationConfig config;
	private final AuthenticationService authService;

	public BrokageApiApplication(ApplicationConfig config, AuthenticationService authService) {
		this.config = config;
		this.authService = authService;
	}

	@PostConstruct
	public void runAfterStartup() {
//		if ("dev".equals(config.getActiveProfile())) {
//			authService.createAdminUser();
//		}
		logger.info("API is ready");
	}

	public static void main(String[] args) {
		SpringApplication.run(BrokageApiApplication.class, args);
	}
}
