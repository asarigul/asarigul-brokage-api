package com.brokage.api.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestHelper.class)
public abstract class BaseIntegrationTest {
	protected static final int FORBIDDEN = HttpStatus.FORBIDDEN.value();
	protected static final int UNAUTHORIZED = HttpStatus.UNAUTHORIZED.value();
	protected static final int CREATED = HttpStatus.CREATED.value();
	protected static final int BAD_REQUEST = HttpStatus.BAD_REQUEST.value();
	protected static final int NOT_FOUND = HttpStatus.NOT_FOUND.value();

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;
}
