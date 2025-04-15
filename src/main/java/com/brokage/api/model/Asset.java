package com.brokage.api.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(
	indexes = { //
		@Index( name = "idx_customer_asset", columnList = "customerId, assetName", unique = true) } //
)
public class Asset {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long customerId;
	private String assetName;
	
	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal size;
	
	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal usableSize;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public String getAssetName() {
		return assetName;
	}

	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	public BigDecimal getSize() {
		return size;
	}

	public void setSize(BigDecimal size) {
		this.size = size;
	}

	public BigDecimal getUsableSize() {
		return usableSize;
	}

	public void setUsableSize(BigDecimal usableSize) {
		this.usableSize = usableSize;
	}
}
