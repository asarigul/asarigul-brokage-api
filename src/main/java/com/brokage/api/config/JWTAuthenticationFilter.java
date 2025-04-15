package com.brokage.api.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.brokage.api.exception.SecurityException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JWTAuthenticationFilter implements Filter {
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";
	private final ApplicationConfig config;
	private final ObjectMapper objectMapper;

	public JWTAuthenticationFilter(ApplicationConfig config, ObjectMapper objectMapper) {
		this.config = config;
		this.objectMapper = objectMapper;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String authorizationHeader = httpRequest.getHeader(AUTHORIZATION_HEADER);

		if (SecurityContextHolder.getContext().getAuthentication() == null && authorizationHeader != null
				&& authorizationHeader.startsWith(BEARER_PREFIX)) {
			String token = authorizationHeader.substring(BEARER_PREFIX.length());

			try {
				Claims claims = validateToken(token);
				String userId = claims.getSubject();
				Boolean isAdmin = claims.get("isAdmin", Boolean.class);

				
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId,
						isAdmin, null);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (Exception e) {
				com.brokage.api.exception.SecurityException se = new SecurityException("Cannot authorize request");
				httpResponse.setStatus(se.getStatus().value());
				httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
				objectMapper.writeValue(response.getWriter(), se.getResponseBody());
				return;
			}
		}

		chain.doFilter(request, response);
	}

	
	private Claims validateToken(String token) {
		Key key = Keys.hmacShaKeyFor(config.getJwtSecretKey().getBytes(StandardCharsets.UTF_8));
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
	}
}
