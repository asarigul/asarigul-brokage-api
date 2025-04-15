package com.brokage.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.brokage.api.model.Order;

public record OrderResponse(Long id, Long customerId, String assetName, Order.Side side, Order.Status status,
		BigDecimal size, BigDecimal price, LocalDateTime createDate) {

	public static OrderResponse from(Order o) {
		return new OrderResponse(
				o.getId(), 
				o.getCustomerId(), 
				o.getAssetName(), 
				o.getSide(), 
				o.getStatus(),
				o.getSize(), 
				o.getPrice(), 
				o.getCreateDate());
	}

}
