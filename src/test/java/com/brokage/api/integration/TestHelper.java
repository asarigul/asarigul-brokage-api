package com.brokage.api.integration;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.brokage.api.dto.LoginRequest;
import com.brokage.api.dto.LoginResponse;
import com.brokage.api.model.Asset;
import com.brokage.api.model.Customer;
import com.brokage.api.repository.AssetRepository;
import com.brokage.api.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@TestComponent
public class TestHelper {

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	private static final String ADMIN = "ADMIN";

	private Customer customer, admin;

	private Asset tryAsset;
	private String token, adminToken;

	public Customer customer() {
		if (customer == null) {
			customer = createCustomer(USERNAME, PASSWORD, false);
		}

		return customer;
	}

	public Customer admin() {
		if (admin == null) {
			admin = createCustomer(ADMIN, ADMIN, true);
		}

		return admin;
	}

	public Asset tryAsset() {
		if (tryAsset == null) {
			tryAsset = createAsset("TRY", BigDecimal.valueOf(1000), customer());
		}

		return tryAsset;
	}

	public String token() throws Exception {
		if (token == null) {
			token = createToken(customer());
		}

		return token;
	}

	public String adminToken() throws Exception {
		if (adminToken == null) {
			adminToken = createToken(admin());
		}

		return adminToken;
	}

	private String createToken(Customer customer) throws UnsupportedEncodingException, Exception {
		LoginRequest loginRequest = new LoginRequest(customer.getUsername(), customer.getPassword());
		String response = post("/api/login", loginRequest, null).andReturn().getResponse().getContentAsString();
		// JsonPath.read(response, "$.token")
		return objectMapper.readValue(response, LoginResponse.class).token();
	}

	public Asset createAsset(String assetName, BigDecimal size, Customer customer) {
		Asset asset = new Asset();
		asset.setCustomer(customer);
		asset.setAssetName(assetName);
		asset.setSize(size);
		asset.setUsableSize(size);
		return assetRepository.save(asset);
	}
	
	public void ensureAssetBalance(String assetName, BigDecimal size) {
		Optional<Asset> tryAsset = getAsset(assetName);
		if(! tryAsset.isPresent()) {
			createAsset(assetName, size, customer());
		} else {
			Asset asset = tryAsset.get();
			asset.setSize(size);
			asset.setUsableSize(size);
			assetRepository.save(asset);			
		}
	}

	public Customer createCustomer(String userName, String password, boolean isAdmin) {
		Customer customer = new Customer();
		customer.setUsername(userName);
		customer.setPassword(password);
		customer.setAdmin(isAdmin);
		return customerRepository.save(customer);
	}

	public Optional<Asset> getAsset(String assetName) {
		return assetRepository.findByCustomerIdAndAssetName(customer.getId(), assetName);
	}

	public ResultActions post(String path, Object request, String token) throws Exception {
		MockHttpServletRequestBuilder post = addToken(MockMvcRequestBuilders.post(path), token);

		return mockMvc.perform(
				post.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)));
	}

	public ResultActions put(String path, Object request, String token) throws Exception {
		MockHttpServletRequestBuilder put = addToken(MockMvcRequestBuilders.put(path), token);

		if (request != null) {
			put = put.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request));
		}

		return mockMvc.perform(put);
	}

	public ResultActions get(String path, String token) throws Exception {
		return mockMvc.perform(addToken(MockMvcRequestBuilders.get(path), token));
	}

	public ResultActions delete(String path, String token) throws Exception {
		return mockMvc.perform(addToken(MockMvcRequestBuilders.delete(path), token));
	}

	private MockHttpServletRequestBuilder addToken(MockHttpServletRequestBuilder builder, String token) {
		if (token != null) {
			builder = builder.header("Authorization", "Bearer " + token);
		}
		return builder;
	}
}
