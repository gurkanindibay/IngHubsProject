package com.ing.walletservice.dto.request;

import com.ing.walletservice.entity.Transaction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class WithdrawRequest {
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Wallet ID is required")
    private Long walletId;
    
    @NotNull(message = "Destination is required")
    private String destination;
    
    @NotNull(message = "Opposite party type is required")
    private Transaction.OppositePartyType oppositePartyType;
    
    // Constructors
    public WithdrawRequest() {}
    
    public WithdrawRequest(BigDecimal amount, Long walletId, String destination,
                          Transaction.OppositePartyType oppositePartyType) {
        this.amount = amount;
        this.walletId = walletId;
        this.destination = destination;
        this.oppositePartyType = oppositePartyType;
    }
    
    // Getters and Setters
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public Long getWalletId() {
        return walletId;
    }
    
    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public Transaction.OppositePartyType getOppositePartyType() {
        return oppositePartyType;
    }
    
    public void setOppositePartyType(Transaction.OppositePartyType oppositePartyType) {
        this.oppositePartyType = oppositePartyType;
    }
}
