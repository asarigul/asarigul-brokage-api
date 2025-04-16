package com.brokage.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brokage.api.dto.AssetResponse;
import com.brokage.api.dto.CreateAssetRequest;
import com.brokage.api.service.AssetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

	private final AssetService assetService;

	public AssetController(AssetService assetService) {
		this.assetService = assetService;
	}
	
	@GetMapping
	public ResponseEntity<List<AssetResponse>> listOwnedAssets() {
		return ResponseEntity.ok(assetService.listAssets());
	}

	@GetMapping("/{customerId}")
	public ResponseEntity<List<AssetResponse>> listAssets(@PathVariable("customerId") Long customerId) {
		return ResponseEntity.ok(assetService.listAssets(customerId));
	}
	
	@PostMapping
	public ResponseEntity<AssetResponse> createAsset(@RequestBody @Valid CreateAssetRequest request) {
		AssetResponse response = assetService.createAsset(request.customerId(), request.assetName(), request.size(), request.usableSize());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
