package com.brokage.api.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.brokage.api.dto.AssetResponse;
import com.brokage.api.exception.AssetAlreadyExistsException;
import com.brokage.api.exception.CustomerNotFoundException;
import com.brokage.api.exception.InvalidArgumentException;
import com.brokage.api.exception.SecurityException;
import com.brokage.api.model.Asset;
import com.brokage.api.model.Customer;
import com.brokage.api.repository.AssetRepository;
import com.brokage.api.repository.CustomerRepository;

@Service
public class AssetService extends BaseService {
	private final AssetRepository assetRepository;
	private final CustomerRepository customerRepository;

	public AssetService(AssetRepository assetRepository, CustomerRepository customerRepository) {
		this.assetRepository = assetRepository;
		this.customerRepository = customerRepository;
	}

	public List<AssetResponse> listAssets() {
		return listAssets(getAuthenticatedCustomerId());
	}

	public List<AssetResponse> listAssets(Long customerId) {
		Long callerId = getAuthenticatedCustomerId();

		if (!callerId.equals(customerId) && !isAdmin()) {
			throw new SecurityException("Only customer or admins can list customer's assets");
		}

		List<Asset> assets = assetRepository.findByCustomerId(customerId);

		return assets.stream().map(AssetResponse::from).collect(Collectors.toList());
	}

	public AssetResponse createAsset(Long customerId, String assetName, BigDecimal size, BigDecimal usableSize) {
		if(! isAdmin()) {
			throw SecurityException.adminRequired();
		}
		
		if(usableSize.compareTo(size) > 0) {
			throw new InvalidArgumentException("usableSize cannot be bigger that size");
		}
		
		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new CustomerNotFoundException(customerId));

		if(assetRepository.findByCustomerAndAssetName(customer, assetName).isPresent()) {
			throw new AssetAlreadyExistsException(customerId, assetName);
		}
		
		Asset asset = new Asset();
		asset.setCustomer(customer);
		asset.setAssetName(assetName);
		asset.setSize(size);
		asset.setUsableSize(usableSize);
		
		assetRepository.save(asset);
		
		logger.info("Asset created by admin {}. Customer: {}, asset: {}, size: {}, usableSize: {}", 
				getAuthenticatedCustomerId(), customerId, assetName, size, usableSize);
		
		return AssetResponse.from(asset);
	}
}
