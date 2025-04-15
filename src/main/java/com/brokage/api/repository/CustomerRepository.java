package com.brokage.api.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brokage.api.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

	Optional<Customer> findByUsernameAndPassword(String username, String password);
	
	List<Customer> findByIsAdmin(boolean isAdmin);
}
