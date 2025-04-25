package com.brokage.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.brokage.api.model.Asset;
import com.brokage.api.model.Customer;

import jakarta.persistence.LockModeType;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
	default List<Asset> findByCustomerId(Long customerId) {
		return findByCustomer(new Customer(customerId));
	}
	
	List<Asset> findByCustomer(Customer customer);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT a FROM Asset a WHERE a.customer = :customer AND a.assetName = :assetName")
	Optional<Asset> findByCustomerAndAssetNameWithLock(@Param("customer") Customer customer,
			@Param("assetName") String assetName);
	
	default Optional<Asset> findByCustomerIdAndAssetName(Long customerId, String assetName) {
		return findByCustomerAndAssetName(new Customer(customerId), assetName);
	}
	
	Optional<Asset> findByCustomerAndAssetName(Customer customer, String assetName);
	
}
