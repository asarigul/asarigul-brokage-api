package com.brokage.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.brokage.api.model.Customer;
import com.brokage.api.model.Order;

import jakarta.persistence.LockModeType;

public interface OrderRepository extends JpaRepository<Order, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT o FROM Order o WHERE o.id = :id AND o.status = :status")
	Optional<Order> findByIdAndStatus(@Param("id") Long id, @Param("status") Order.Status status);

	default List<Order> findByCustomerIdAndCreateDateBetween(Long customerId, LocalDateTime start, LocalDateTime end) {
		return findByCustomerAndCreateDateBetween(new Customer(customerId), start, end);
	}
	
	List<Order> findByCustomerAndCreateDateBetween(Customer customer, LocalDateTime start, LocalDateTime end);
}
