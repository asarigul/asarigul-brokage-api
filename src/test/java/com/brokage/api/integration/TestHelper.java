package com.brokage.api.integration;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.brokage.api.config.ApplicationConfig;
import com.brokage.api.dto.LoginRequest;
import com.brokage.api.dto.LoginResponse;
import com.brokage.api.model.Asset;
import com.brokage.api.model.Customer;
import com.brokage.api.repository.AssetRepository;
import com.brokage.api.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

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
	
	@Autowired
	private ApplicationConfig config;
	
	private Customer customer, admin;

	private Asset tryAsset;
	private String token, adminToken;
	
	@PostConstruct
	public void init() throws Exception {
		List<Customer> users = config.getPredefinedUsers();
		
		admin = users.stream().filter(Customer::isAdmin).findFirst().get();
		admin.setId(customerRepository.findByUsername(admin.getUsername()).get().getId());
		
		customer = users.stream().filter(c -> !c.isAdmin()).findFirst().get();
		customer.setId(customerRepository.findByUsername(customer.getUsername()).get().getId());
		
		tryAsset = createAsset("TRY", BigDecimal.valueOf(1000), customer);
		
		token = createToken(customer);
		adminToken = createToken(admin);
	}

	public Customer customer() {
		return customer;
	}

	public Customer admin() {
		return admin;
	}

	public Asset tryAsset() {
		return tryAsset;
	}

	public String token() throws Exception {
		return token;
	}

	public String adminToken() throws Exception {
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
