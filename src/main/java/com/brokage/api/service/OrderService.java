package com.brokage.api.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.brokage.api.dto.OrderResponse;
import com.brokage.api.exception.AssetNotFoundException;
import com.brokage.api.exception.InsufficientBalanceException;
import com.brokage.api.exception.OrderNotFoundException;
import com.brokage.api.exception.SecurityException;
import com.brokage.api.model.Asset;
import com.brokage.api.model.Customer;
import com.brokage.api.model.Order;
import com.brokage.api.model.OrderSide;
import com.brokage.api.model.OrderStatus;
import com.brokage.api.repository.AssetRepository;
import com.brokage.api.repository.OrderRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderService {
	private static final String TRY = "TRY";
	private final OrderRepository orderRepository;
	private final AssetRepository assetRepository;
	private final AuthenticationService authService;

	public OrderService(OrderRepository orderRepository, AssetRepository assetRepository, AuthenticationService authService) {
		this.orderRepository = orderRepository;
		this.assetRepository = assetRepository;
		this.authService = authService;
	}

	@Transactional
	public OrderResponse createOrder(OrderSide side, String assetName, BigDecimal size, BigDecimal price) {
		final Long customerId = authService.getAuthenticatedCustomerId();
		final BigDecimal totalAmount = price.multiply(size);
		
		Asset tryAsset = assetRepository.findByCustomerIdAndAssetNameWithLock(customerId, TRY)
				.orElseThrow(() -> new AssetNotFoundException(customerId, TRY));
		
		Asset otherAsset = assetRepository.findByCustomerIdAndAssetNameWithLock(customerId, assetName)
	            .orElse(null);
		
		if(side == OrderSide.SELL) {
			if(otherAsset == null) {
				throw new AssetNotFoundException(customerId, assetName);
			}
			
			if (otherAsset.getUsableSize().compareTo(size) < 0) {
				throw new InsufficientBalanceException(customerId, size, assetName);
			}
			otherAsset.setUsableSize(otherAsset.getUsableSize().subtract(size));
			tryAsset.setSize(tryAsset.getSize().add(totalAmount));
		} else {
			if(tryAsset.getUsableSize().compareTo(totalAmount) < 0) {				
				throw new InsufficientBalanceException(customerId, totalAmount, TRY);
			}
			tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(totalAmount));
			
			if(otherAsset == null) {
				otherAsset = new Asset();
				otherAsset.setAssetName(assetName);
				otherAsset.setCustomer(new Customer(customerId));
				otherAsset.setSize(BigDecimal.ZERO);
				otherAsset.setUsableSize(BigDecimal.ZERO);
			} 
			
			otherAsset.setSize(otherAsset.getSize().add(size));
		}
		
		tryAsset = assetRepository.save(tryAsset);
		otherAsset = assetRepository.save(otherAsset);

		Order order = new Order();
		order.setCustomer(new Customer(customerId));
		order.setStatus(OrderStatus.PENDING);
		order.setCreateDate(LocalDateTime.now());
		order.setAssetName(assetName);
		order.setSide(side);
		order.setSize(size);
		order.setPrice(price);

		order = orderRepository.save(order);
		
		return OrderResponse.from(order);
	}
	

	@Transactional
	public OrderResponse deleteOrder(Long orderId) {
		final Long customerId = authService.getAuthenticatedCustomerId();
		
		Order order = orderRepository.findByIdAndStatus(orderId, OrderStatus.PENDING)
				.orElseThrow(() -> new OrderNotFoundException(orderId));

		if (customerId != order.getCustomerId()) {
			throw new SecurityException("Order is not owned by the current user");
		}

		Asset tryAsset = assetRepository.findByCustomerIdAndAssetNameWithLock(customerId, TRY)
				.orElseThrow(() -> new AssetNotFoundException(customerId, TRY));
		
		Asset otherAsset = assetRepository.findByCustomerIdAndAssetNameWithLock(customerId, order.getAssetName())
				.orElseThrow(() -> new AssetNotFoundException(customerId, order.getAssetName()));
		
		
		final BigDecimal size = order.getSize();
		final BigDecimal totalAmount = order.getPrice().multiply(size);
		
		if(order.getSide() == OrderSide.SELL) {
			otherAsset.setUsableSize(otherAsset.getUsableSize().add(size));
			tryAsset.setSize(tryAsset.getSize().subtract(totalAmount));
		} else {
			otherAsset.setSize(otherAsset.getSize().subtract(size));
			tryAsset.setUsableSize(tryAsset.getUsableSize().add(totalAmount));
		}
		
		tryAsset = assetRepository.save(tryAsset);
		otherAsset = assetRepository.save(otherAsset);
		
		order.setStatus(OrderStatus.CANCELED);
		orderRepository.save(order);
		
		return OrderResponse.from(order);
	}
	

	@Transactional
	public OrderResponse matchOrder(Long orderId) {
		if (!authService.isAdmin()) {
			throw new SecurityException("Admin privileges required");
		}
		
		Order order = orderRepository.findByIdAndStatus(orderId, OrderStatus.PENDING)
				.orElseThrow(() -> new OrderNotFoundException(orderId));

		Long customerId = order.getCustomerId();
	
		Asset tryAsset = assetRepository.findByCustomerIdAndAssetNameWithLock(customerId, TRY)
				.orElseThrow(() -> new AssetNotFoundException(customerId, TRY));
		
		Asset otherAsset = assetRepository.findByCustomerIdAndAssetNameWithLock(customerId, order.getAssetName())
				.orElseThrow(() -> new AssetNotFoundException(customerId, order.getAssetName()));
		
		
		final BigDecimal size = order.getSize();
		final BigDecimal totalAmount = order.getPrice().multiply(size);
		
		if(order.getSide() == OrderSide.SELL) {
			otherAsset.setSize(otherAsset.getSize().subtract(size));
			tryAsset.setUsableSize(tryAsset.getUsableSize().add(totalAmount));
		} else {
			otherAsset.setUsableSize(otherAsset.getUsableSize().add(size));
			tryAsset.setSize(tryAsset.getSize().subtract(totalAmount));
		}
		
		assetRepository.save(tryAsset);
		assetRepository.save(otherAsset);
		
		order.setStatus(OrderStatus.MATCHED);
		orderRepository.save(order);
		
		return OrderResponse.from(order);
	}

	public List<OrderResponse> listOrders(LocalDateTime startDate, LocalDateTime endDate) {
		Long customerId = authService.getAuthenticatedCustomerId();
		if (endDate == null) {
			endDate = LocalDateTime.now();
		}

		if (startDate == null) {
			startDate = endDate.minus(Duration.ofDays(365));
		}

		List<Order> orders = orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);
		
		return orders.stream().map(OrderResponse::from).collect(Collectors.toList());
	}
}
