package com.brokage.api.exception;

import org.springframework.http.HttpStatus;

public class AssetAlreadyExistsException extends BrokageApiException  {
	private static final long serialVersionUID = -8722614514285134358L;

	public AssetAlreadyExistsException(Long customerId, String assetName) {
        super(String.format("Asset: %s already exists for customer: %d", assetName, customerId));
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
