package com.brokage.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.brokage.api.model.Asset;

import jakarta.persistence.LockModeType;

public interface AssetRepository extends JpaRepository<Asset, Long> {
	List<Asset> findByCustomerId(Long customerId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT a FROM Asset a WHERE a.customerId = :customerId AND a.assetName = :assetName")
	Optional<Asset> findByCustomerIdAndAssetNameWithLock(@Param("customerId") Long customerId,
			@Param("assetName") String assetName);
	
	Optional<Asset> findByCustomerIdAndAssetName(Long customerId, String assetName);
}
