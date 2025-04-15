package com.brokage.api.exception;

import org.springframework.http.HttpStatus;

public class AssetNotFoundException extends BrokageApiException  {
	private static final long serialVersionUID = 4882858803523020253L;

	public AssetNotFoundException(Long customerId, String assetName) {
        super(String.format("Asset: %s not found for customer: %d", assetName, customerId));
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
