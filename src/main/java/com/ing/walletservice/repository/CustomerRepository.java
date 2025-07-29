package com.ing.walletservice.repository;

import com.ing.walletservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByUsername(String username);
    
    Optional<Customer> findByTckn(String tckn);
    
    boolean existsByUsername(String username);
    
    boolean existsByTckn(String tckn);
}
