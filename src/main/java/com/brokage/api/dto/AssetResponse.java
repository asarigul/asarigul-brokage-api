package com.brokage.api.dto;

import java.math.BigDecimal;

import com.brokage.api.model.Asset;

public record AssetResponse(Long id, Long customerId, String assetName, BigDecimal size, BigDecimal usableSize) {

	public static AssetResponse from(Asset a) {
		return new AssetResponse(a.getId(), a.getCustomerId(), a.getAssetName(), a.getSize(), a.getUsableSize());
	}
}
