package com.brokage.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brokage.api.dto.AssetResponse;
import com.brokage.api.service.AssetService;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

	private final AssetService assetService;

	public AssetController(AssetService assetService) {
		this.assetService = assetService;
	}
	
	@GetMapping
	public ResponseEntity<List<AssetResponse>> listOrdersOfCustomer() {
		return ResponseEntity.ok(assetService.listAssets());
	}

	@GetMapping("/{customerId}")
	public ResponseEntity<List<AssetResponse>> listOrders(@PathVariable("customerId") Long customerId) {
		return ResponseEntity.ok(assetService.listAssets(customerId));
	}
}
