package com.brokage.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableAspectJAutoProxy
public class BrokageApiApplication {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void runAfterStartup() {
		logger.info("API is ready");
	}

	public static void main(String[] args) {
		SpringApplication.run(BrokageApiApplication.class, args);
	}
}
