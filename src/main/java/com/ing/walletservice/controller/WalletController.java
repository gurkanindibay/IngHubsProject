package com.ing.walletservice.controller;

import com.ing.walletservice.dto.request.CreateWalletRequest;
import com.ing.walletservice.dto.response.ApiResponse;
import com.ing.walletservice.dto.response.WalletResponse;
import com.ing.walletservice.entity.Wallet;
import com.ing.walletservice.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@Tag(name = "Wallet", description = "Wallet management API")
@SecurityRequirement(name = "Bearer Authentication")
public class WalletController {
    
    private final WalletService walletService;
    
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }
    
    @PostMapping
    @Operation(summary = "Create a new wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(
            @Valid @RequestBody CreateWalletRequest request,
            Authentication authentication) {
        WalletResponse wallet = walletService.createWallet(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Wallet created successfully", wallet));
    }
    
    @GetMapping
    @Operation(summary = "List wallets for a customer")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> listWallets(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Wallet.Currency currency,
            @RequestParam(required = false) BigDecimal minBalance,
            Authentication authentication) {
        List<WalletResponse> wallets = walletService.listWallets(customerId, currency, minBalance, authentication);
        return ResponseEntity.ok(ApiResponse.success("Wallets retrieved successfully", wallets));
    }
    
    @GetMapping("/{walletId}")
    @Operation(summary = "Get wallet by ID")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(
            @PathVariable Long walletId,
            Authentication authentication) {
        WalletResponse wallet = walletService.getWallet(walletId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Wallet retrieved successfully", wallet));
    }
}
