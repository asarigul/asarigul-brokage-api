package com.brokage.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.brokage.api.dto.AssetResponse;
import com.brokage.api.exception.SecurityException;
import com.brokage.api.model.Asset;
import com.brokage.api.repository.AssetRepository;

@Service
public class AssetService {
	private final AuthenticationService authService;
	private final AssetRepository assetRepository;

	public AssetService(AssetRepository assetRepository, AuthenticationService authService) {
		this.assetRepository = assetRepository;
		this.authService = authService;
	}

	public List<AssetResponse> listAssets() {
		Long customerId = authService.getAuthenticatedCustomerId();
		return listAssets(customerId);
	}

	public List<AssetResponse> listAssets(Long customerId) {
		Long callerId = authService.getAuthenticatedCustomerId();

		if (!callerId.equals(customerId) && !authService.isAdmin()) {
			throw new SecurityException("Only customer or admins can list customer's assets");
		}

		List<Asset> assets = assetRepository.findByCustomerId(customerId);

		return assets.stream().map(AssetResponse::from).collect(Collectors.toList());
	}

}
