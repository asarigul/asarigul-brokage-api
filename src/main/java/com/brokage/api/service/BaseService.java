package com.brokage.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.brokage.api.exception.SecurityException;

public class BaseService {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected final Long getAuthenticatedCustomerId() {
		return Long.valueOf(ensureAuthentication().getPrincipal().toString());
	}

	protected final boolean isAdmin() {
		return (Boolean) ensureAuthentication().getCredentials();
	}
	
	private static Authentication ensureAuthentication() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null) {
			throw new SecurityException("Not authenticated!");
		}
		return authentication;
	}
}
