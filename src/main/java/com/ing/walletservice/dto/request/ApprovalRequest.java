package com.ing.walletservice.dto.request;

import com.ing.walletservice.entity.Transaction;
import jakarta.validation.constraints.NotNull;

public class ApprovalRequest {
    
    @NotNull(message = "Transaction ID is required")
    private Long transactionId;
    
    @NotNull(message = "Status is required")
    private Transaction.TransactionStatus status;
    
    // Constructors
    public ApprovalRequest() {}
    
    public ApprovalRequest(Long transactionId, Transaction.TransactionStatus status) {
        this.transactionId = transactionId;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }
    
    public Transaction.TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(Transaction.TransactionStatus status) {
        this.status = status;
    }
}
