package com.ing.walletservice.dto.response;

import com.ing.walletservice.entity.Wallet;

import java.math.BigDecimal;

public class WalletResponse {
    
    private Long id;
    private String walletName;
    private Wallet.Currency currency;
    private Boolean activeForShopping;
    private Boolean activeForWithdraw;
    private BigDecimal balance;
    private BigDecimal usableBalance;
    private Long customerId;
    
    // Constructors
    public WalletResponse() {}
    
    public WalletResponse(Wallet wallet) {
        this.id = wallet.getId();
        this.walletName = wallet.getWalletName();
        this.currency = wallet.getCurrency();
        this.activeForShopping = wallet.getActiveForShopping();
        this.activeForWithdraw = wallet.getActiveForWithdraw();
        this.balance = wallet.getBalance();
        this.usableBalance = wallet.getUsableBalance();
        this.customerId = wallet.getCustomer().getId();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getWalletName() {
        return walletName;
    }
    
    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }
    
    public Wallet.Currency getCurrency() {
        return currency;
    }
    
    public void setCurrency(Wallet.Currency currency) {
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
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
