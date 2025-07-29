package com.ing.walletservice.repository;

import com.ing.walletservice.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    List<Wallet> findByCustomerId(Long customerId);
    
    @Query("SELECT w FROM Wallet w WHERE w.customer.id = :customerId " +
           "AND (:currency IS NULL OR w.currency = :currency) " +
           "AND (:minBalance IS NULL OR w.balance >= :minBalance)")
    List<Wallet> findByCustomerIdWithFilters(@Param("customerId") Long customerId,
                                            @Param("currency") Wallet.Currency currency,
                                            @Param("minBalance") BigDecimal minBalance);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") Long id);
}
