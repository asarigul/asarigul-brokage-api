package com.brokage.api.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brokage.api.dto.OrderRequest;
import com.brokage.api.dto.OrderResponse;
import com.brokage.api.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderRequest request) {
		OrderResponse order = orderService.createOrder(request.side(), request.assetName(), request.size(), request.price());
		return ResponseEntity.status(HttpStatus.CREATED).body(order);
	}

	@GetMapping
	public ResponseEntity<List<OrderResponse>> listOrders(
			@RequestParam(name= "startDate", required = false) LocalDateTime startDate, 
			@RequestParam(name = "endDate", required = false) LocalDateTime endDate) {
		return ResponseEntity.ok(orderService.listOrders(startDate, endDate));
	}

	@DeleteMapping("/{orderId}")
	public ResponseEntity<OrderResponse> deleteOrder(@PathVariable("orderId") Long orderId) {
		return ResponseEntity.ok(orderService.deleteOrder(orderId));
	}

	@PutMapping("/{orderId}")
	public ResponseEntity<OrderResponse> matchOrder(@PathVariable("orderId") Long orderId) {
		return ResponseEntity.ok(orderService.matchOrder(orderId));
	}
}
