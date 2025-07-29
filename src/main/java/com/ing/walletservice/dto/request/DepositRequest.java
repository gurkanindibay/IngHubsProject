package com.ing.walletservice.dto.request;

import com.ing.walletservice.entity.Transaction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class DepositRequest {
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Wallet ID is required")
    private Long walletId;
    
    @NotNull(message = "Source is required")
    private String source;
    
    @NotNull(message = "Opposite party type is required")
    private Transaction.OppositePartyType oppositePartyType;
    
    // Constructors
    public DepositRequest() {}
    
    public DepositRequest(BigDecimal amount, Long walletId, String source,
                         Transaction.OppositePartyType oppositePartyType) {
        this.amount = amount;
        this.walletId = walletId;
        this.source = source;
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
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public Transaction.OppositePartyType getOppositePartyType() {
        return oppositePartyType;
    }
    
    public void setOppositePartyType(Transaction.OppositePartyType oppositePartyType) {
        this.oppositePartyType = oppositePartyType;
    }
}
