package com.ing.walletservice.dto.response;

import com.ing.walletservice.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    
    private Long id;
    private Long walletId;
    private BigDecimal amount;
    private Transaction.TransactionType type;
    private Transaction.OppositePartyType oppositePartyType;
    private String oppositeParty;
    private Transaction.TransactionStatus status;
    private LocalDateTime createdDate;
    private LocalDateTime processedDate;
    
    // Constructors
    public TransactionResponse() {}
    
    public TransactionResponse(Transaction transaction) {
        this.id = transaction.getId();
        this.walletId = transaction.getWallet().getId();
        this.amount = transaction.getAmount();
        this.type = transaction.getType();
        this.oppositePartyType = transaction.getOppositePartyType();
        this.oppositeParty = transaction.getOppositeParty();
        this.status = transaction.getStatus();
        this.createdDate = transaction.getCreatedDate();
        this.processedDate = transaction.getProcessedDate();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getWalletId() {
        return walletId;
    }
    
    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public Transaction.TransactionType getType() {
        return type;
    }
    
    public void setType(Transaction.TransactionType type) {
        this.type = type;
    }
    
    public Transaction.OppositePartyType getOppositePartyType() {
        return oppositePartyType;
    }
    
    public void setOppositePartyType(Transaction.OppositePartyType oppositePartyType) {
        this.oppositePartyType = oppositePartyType;
    }
    
    public String getOppositeParty() {
        return oppositeParty;
    }
    
    public void setOppositeParty(String oppositeParty) {
        this.oppositeParty = oppositeParty;
    }
    
    public Transaction.TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(Transaction.TransactionStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getProcessedDate() {
        return processedDate;
    }
    
    public void setProcessedDate(LocalDateTime processedDate) {
        this.processedDate = processedDate;
    }
}
