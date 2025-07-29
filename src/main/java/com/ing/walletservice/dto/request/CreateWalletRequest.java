package com.ing.walletservice.dto.request;

import com.ing.walletservice.entity.Wallet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateWalletRequest {

    private Long customerId;
    
    @NotBlank(message = "Wallet name is required")
    private String walletName;
    
    @NotNull(message = "Currency is required")
    private Wallet.Currency currency;
    
    @NotNull(message = "Active for shopping flag is required")
    private Boolean activeForShopping;
    
    @NotNull(message = "Active for withdraw flag is required")
    private Boolean activeForWithdraw;
    
    // Constructors
    public CreateWalletRequest() {}
    public CreateWalletRequest(Long customerId, String walletName, Wallet.Currency currency,
                               Boolean activeForShopping, Boolean activeForWithdraw) {
        this.customerId = customerId;
        this.walletName = walletName;
        this.currency = currency;
        this.activeForShopping = activeForShopping;
        this.activeForWithdraw = activeForWithdraw;
    }
    public CreateWalletRequest(String walletName, Wallet.Currency currency,
                              Boolean activeForShopping, Boolean activeForWithdraw) {
        this.walletName = walletName;
        this.currency = currency;
        this.activeForShopping = activeForShopping;
        this.activeForWithdraw = activeForWithdraw;
    }
    
    // Getters and Setters
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

    public Long getCustomerId() {
        return customerId;
    }
}
