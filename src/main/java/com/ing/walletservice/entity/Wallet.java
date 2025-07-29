package com.ing.walletservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "wallets")
public class Wallet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @NotBlank(message = "Wallet name is required")
    @Column(nullable = false, length = 100)
    private String walletName;
    
    @NotNull(message = "Currency is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;
    
    @NotNull(message = "Active for shopping flag is required")
    @Column(nullable = false)
    private Boolean activeForShopping;
    
    @NotNull(message = "Active for withdraw flag is required")
    @Column(nullable = false)
    private Boolean activeForWithdraw;
    
    @NotNull(message = "Balance is required")
    @PositiveOrZero(message = "Balance must be positive or zero")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @NotNull(message = "Usable balance is required")
    @PositiveOrZero(message = "Usable balance must be positive or zero")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal usableBalance = BigDecimal.ZERO;
    
    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;
    
    // Constructors
    public Wallet() {}
    
    public Wallet(Customer customer, String walletName, Currency currency, 
                  Boolean activeForShopping, Boolean activeForWithdraw) {
        this.customer = customer;
        this.walletName = walletName;
        this.currency = currency;
        this.activeForShopping = activeForShopping;
        this.activeForWithdraw = activeForWithdraw;
        this.balance = BigDecimal.ZERO;
        this.usableBalance = BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public String getWalletName() {
        return walletName;
    }
    
    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }
    
    public Currency getCurrency() {
        return currency;
    }
    
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
    
    public Boolean getActiveForShopping() {
        return activeForShopping;
    }
    
    public void setActiveForShopping(Boolean activeForShopping) {
        this.activeForShopping = activeForShopping;
    }
    
    public Boolean getActiveForWithdraw() {
        return activeForWithdraw;
    }
    
    public void setActiveForWithdraw(Boolean activeForWithdraw) {
        this.activeForWithdraw = activeForWithdraw;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public BigDecimal getUsableBalance() {
        return usableBalance;
    }
    
    public void setUsableBalance(BigDecimal usableBalance) {
        this.usableBalance = usableBalance;
    }
    
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
    
    public enum Currency {
        TRY, USD, EUR
    }
}
