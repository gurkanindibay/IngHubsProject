package com.ing.walletservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Wallet is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    
    @NotNull(message = "Opposite party type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OppositePartyType oppositePartyType;
    
    @NotNull(message = "Opposite party is required")
    @Column(nullable = false)
    private String oppositeParty;
    
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;
    
    @NotNull(message = "Created date is required")
    @Column(nullable = false)
    private LocalDateTime createdDate;
    
    @Column
    private LocalDateTime processedDate;
    
    // Constructors
    public Transaction() {
        this.createdDate = LocalDateTime.now();
    }
    
    public Transaction(Wallet wallet, BigDecimal amount, TransactionType type,
                      OppositePartyType oppositePartyType, String oppositeParty,
                      TransactionStatus status) {
        this.wallet = wallet;
        this.amount = amount;
        this.type = type;
        this.oppositePartyType = oppositePartyType;
        this.oppositeParty = oppositeParty;
        this.status = status;
        this.createdDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Wallet getWallet() {
        return wallet;
    }
    
    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public TransactionType getType() {
        return type;
    }
    
    public void setType(TransactionType type) {
        this.type = type;
    }
    
    public OppositePartyType getOppositePartyType() {
        return oppositePartyType;
    }
    
    public void setOppositePartyType(OppositePartyType oppositePartyType) {
        this.oppositePartyType = oppositePartyType;
    }
    
    public String getOppositeParty() {
        return oppositeParty;
    }
    
    public void setOppositeParty(String oppositeParty) {
        this.oppositeParty = oppositeParty;
    }
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(TransactionStatus status) {
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
    
    public enum TransactionType {
        DEPOSIT, WITHDRAW
    }
    
    public enum OppositePartyType {
        IBAN, PAYMENT
    }
    
    public enum TransactionStatus {
        PENDING, APPROVED, DENIED
    }
}
