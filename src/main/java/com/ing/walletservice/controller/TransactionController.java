package com.ing.walletservice.controller;

import com.ing.walletservice.dto.request.ApprovalRequest;
import com.ing.walletservice.dto.request.DepositRequest;
import com.ing.walletservice.dto.request.WithdrawRequest;
import com.ing.walletservice.dto.response.ApiResponse;
import com.ing.walletservice.dto.response.TransactionResponse;
import com.ing.walletservice.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction", description = "Transaction management API")
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionController {
    
    @Autowired
    private TransactionService transactionService;
    
    @PostMapping("/deposit")
    @Operation(summary = "Make a deposit to a wallet")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @Valid @RequestBody DepositRequest request,
            Authentication authentication) {
        TransactionResponse transaction = transactionService.deposit(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deposit processed successfully", transaction));
    }
    
    @PostMapping("/withdraw")
    @Operation(summary = "Make a withdrawal from a wallet")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            Authentication authentication) {
        TransactionResponse transaction = transactionService.withdraw(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Withdrawal processed successfully", transaction));
    }
    
    @GetMapping("/wallet/{walletId}")
    @Operation(summary = "List transactions for a wallet")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> listTransactions(
            @PathVariable Long walletId,
            Authentication authentication) {
        List<TransactionResponse> transactions = transactionService.listTransactions(walletId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", transactions));
    }
    
    @PostMapping("/approve")
    @Operation(summary = "Approve or deny a pending transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> approveTransaction(
            @Valid @RequestBody ApprovalRequest request,
            Authentication authentication) {
        TransactionResponse transaction = transactionService.approveTransaction(request, authentication);
        return ResponseEntity.ok(ApiResponse.success("Transaction approval processed successfully", transaction));
    }
}
