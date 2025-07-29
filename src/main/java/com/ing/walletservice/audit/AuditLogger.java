package com.ing.walletservice.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AuditLogger {
    
    private static final Logger auditLog = LoggerFactory.getLogger("com.ing.walletservice.audit");
    
    public void logWalletCreation(Long customerId, String walletName, String currency, String username) {
        MDC.put("eventType", "WALLET_CREATION");
        MDC.put("customerId", String.valueOf(customerId));
        MDC.put("walletName", walletName);
        MDC.put("currency", currency);
        MDC.put("username", username);
        
        auditLog.info("Wallet created: {} for customer {} by user {}", walletName, customerId, username);
        
        MDC.clear();
    }
    
    public void logTransactionCreation(Long transactionId, String type, BigDecimal amount, 
                                     Long walletId, String status, String username) {
        MDC.put("eventType", "TRANSACTION_CREATION");
        MDC.put("transactionId", String.valueOf(transactionId));
        MDC.put("transactionType", type);
        MDC.put("amount", amount.toString());
        MDC.put("walletId", String.valueOf(walletId));
        MDC.put("status", status);
        MDC.put("username", username);
        
        auditLog.info("Transaction created: {} {} for wallet {} with status {} by user {}", 
                     type, amount, walletId, status, username);
        
        MDC.clear();
    }
    
    public void logTransactionApproval(Long transactionId, String oldStatus, String newStatus, 
                                     BigDecimal amount, String username) {
        MDC.put("eventType", "TRANSACTION_APPROVAL");
        MDC.put("transactionId", String.valueOf(transactionId));
        MDC.put("oldStatus", oldStatus);
        MDC.put("newStatus", newStatus);
        MDC.put("amount", amount.toString());
        MDC.put("username", username);
        
        auditLog.info("Transaction {} status changed from {} to {} (amount: {}) by user {}", 
                     transactionId, oldStatus, newStatus, amount, username);
        
        MDC.clear();
    }
    
    public void logAuthenticationSuccess(String username, String role) {
        MDC.put("eventType", "AUTH_SUCCESS");
        MDC.put("username", username);
        MDC.put("role", role);
        
        auditLog.info("User {} successfully authenticated with role {}", username, role);
        
        MDC.clear();
    }
    
    public void logAuthenticationFailure(String username, String reason) {
        MDC.put("eventType", "AUTH_FAILURE");
        MDC.put("username", username);
        MDC.put("reason", reason);
        
        auditLog.warn("Authentication failed for user {}: {}", username, reason);
        
        MDC.clear();
    }
    
    public void logUnauthorizedAccess(String username, String resource, String action) {
        MDC.put("eventType", "UNAUTHORIZED_ACCESS");
        MDC.put("username", username);
        MDC.put("resource", resource);
        MDC.put("action", action);
        
        auditLog.warn("Unauthorized access attempt by user {} to {} for action {}", 
                     username, resource, action);
        
        MDC.clear();
    }
    
    public void logBalanceChange(Long walletId, BigDecimal oldBalance, BigDecimal newBalance, 
                                BigDecimal oldUsableBalance, BigDecimal newUsableBalance, 
                                String reason, String username) {
        MDC.put("eventType", "BALANCE_CHANGE");
        MDC.put("walletId", String.valueOf(walletId));
        MDC.put("oldBalance", oldBalance.toString());
        MDC.put("newBalance", newBalance.toString());
        MDC.put("oldUsableBalance", oldUsableBalance.toString());
        MDC.put("newUsableBalance", newUsableBalance.toString());
        MDC.put("reason", reason);
        MDC.put("username", username);
        
        auditLog.info("Wallet {} balance changed: {} -> {} (usable: {} -> {}) due to {} by user {}", 
                     walletId, oldBalance, newBalance, oldUsableBalance, newUsableBalance, reason, username);
        
        MDC.clear();
    }
}
