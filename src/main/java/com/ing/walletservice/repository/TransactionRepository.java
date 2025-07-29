package com.ing.walletservice.repository;

import com.ing.walletservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByWalletIdOrderByCreatedDateDesc(Long walletId);
    
    List<Transaction> findByWalletCustomerIdOrderByCreatedDateDesc(Long customerId);
    
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
}
