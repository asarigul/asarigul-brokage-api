package com.brokage.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.brokage.api.dto.AssetResponse;
import com.brokage.api.exception.SecurityException;
import com.brokage.api.model.Asset;
import com.brokage.api.repository.AssetRepository;

@Service
public class AssetService extends BaseService {
	private final AssetRepository assetRepository;

	public AssetService(AssetRepository assetRepository) {
		this.assetRepository = assetRepository;
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
}
