package com.ing.walletservice.service;

import com.ing.walletservice.audit.AuditLogger;
import com.ing.walletservice.dto.request.ApprovalRequest;
import com.ing.walletservice.dto.request.DepositRequest;
import com.ing.walletservice.dto.request.WithdrawRequest;
import com.ing.walletservice.dto.response.TransactionResponse;
import com.ing.walletservice.entity.Transaction;
import com.ing.walletservice.entity.Wallet;
import com.ing.walletservice.exception.InsufficientBalanceException;
import com.ing.walletservice.exception.ResourceNotFoundException;
import com.ing.walletservice.exception.UnauthorizedException;
import com.ing.walletservice.exception.WalletNotActiveException;
import com.ing.walletservice.repository.TransactionRepository;
import com.ing.walletservice.repository.WalletRepository;
import com.ing.walletservice.security.UserPrincipal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AuditLogger auditLogger;
    
    private static final BigDecimal APPROVAL_THRESHOLD = new BigDecimal("1000");
    
    public TransactionService(TransactionRepository transactionRepository, WalletRepository walletRepository, AuditLogger auditLogger) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.auditLogger = auditLogger;
    }
    
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public TransactionResponse deposit(DepositRequest request, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        boolean isEmployee = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
        
        logger.info("Processing deposit request: amount={}, walletId={}, user={}", 
                   request.getAmount(), request.getWalletId(), userPrincipal.getUsername());
        
        // Use pessimistic locking to get the latest wallet state
        Wallet wallet = walletRepository.findByIdForUpdate(request.getWalletId())
                .orElseThrow(() -> {
                    logger.error("Wallet not found with ID: {}", request.getWalletId());
                    return new ResourceNotFoundException("Wallet not found");
                });
        
        // Check if user can access this wallet
        if (!isEmployee && !wallet.getCustomer().getId().equals(userPrincipal.getId())) {
            logger.warn("User {} attempted to deposit to wallet {} owned by customer {}", 
                       userPrincipal.getUsername(), request.getWalletId(), wallet.getCustomer().getId());
            auditLogger.logUnauthorizedAccess(userPrincipal.getUsername(), 
                                            "wallet", "deposit to wallet " + request.getWalletId());
            throw new UnauthorizedException("You can only deposit to your own wallets");
        }
        
        // Determine transaction status based on amount
        Transaction.TransactionStatus status = request.getAmount().compareTo(APPROVAL_THRESHOLD) >= 0 
                ? Transaction.TransactionStatus.PENDING 
                : Transaction.TransactionStatus.APPROVED;
        
        logger.debug("Deposit transaction status determined: {} for amount {}", status, request.getAmount());
        
        // Capture old balances for audit logging
        BigDecimal oldBalance = wallet.getBalance();
        BigDecimal oldUsableBalance = wallet.getUsableBalance();
        
        logger.debug("Inside lock for deposit: walletId={}, oldBalance={}, oldUsableBalance={}", 
                    wallet.getId(), oldBalance, oldUsableBalance);
        
        Transaction transaction = new Transaction(
                wallet,
                request.getAmount(),
                Transaction.TransactionType.DEPOSIT,
                request.getOppositePartyType(),
                request.getSource(),
                status
        );
        
        transaction = transactionRepository.save(transaction);
        
        // Update wallet balances
        updateWalletBalancesForDeposit(wallet, request.getAmount(), status);
        
        logger.debug("After balance update: walletId={}, newBalance={}, newUsableBalance={}", 
                    wallet.getId(), wallet.getBalance(), wallet.getUsableBalance());
        
        auditLogger.logTransactionCreation(transaction.getId(), "DEPOSIT", request.getAmount(), 
                                          request.getWalletId(), status.name(), userPrincipal.getUsername());
        
        auditLogger.logBalanceChange(wallet.getId(), oldBalance, wallet.getBalance(), 
                                   oldUsableBalance, wallet.getUsableBalance(), 
                                   "Deposit transaction", userPrincipal.getUsername());
        
        logger.info("Deposit transaction {} created successfully with status {} for wallet {}", 
                   transaction.getId(), status, request.getWalletId());
        
        return new TransactionResponse(transaction);
    }
    
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public TransactionResponse withdraw(WithdrawRequest request, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        boolean isEmployee = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
        
        logger.info("Processing withdrawal request: amount={}, walletId={}, user={}", 
                   request.getAmount(), request.getWalletId(), userPrincipal.getUsername());
        
        // Use pessimistic locking to get the latest wallet state
        Wallet wallet = walletRepository.findByIdForUpdate(request.getWalletId())
                .orElseThrow(() -> {
                    logger.error("Wallet not found with ID: {}", request.getWalletId());
                    return new ResourceNotFoundException("Wallet not found");
                });
        
        // Check if user can access this wallet
        if (!isEmployee && !wallet.getCustomer().getId().equals(userPrincipal.getId())) {
            logger.warn("User {} attempted to withdraw from wallet {} owned by customer {}", 
                       userPrincipal.getUsername(), request.getWalletId(), wallet.getCustomer().getId());
            auditLogger.logUnauthorizedAccess(userPrincipal.getUsername(), 
                                            "wallet", "withdraw from wallet " + request.getWalletId());
            throw new UnauthorizedException("You can only withdraw from your own wallets");
        }
        
        // Check wallet settings
        if (!wallet.getActiveForWithdraw()) {
            logger.warn("Withdrawal attempted from inactive wallet {} by user {}", 
                       request.getWalletId(), userPrincipal.getUsername());
            throw new WalletNotActiveException("Wallet is not active for withdrawals");
        }
        
        // Check if sufficient usable balance
        if (wallet.getUsableBalance().compareTo(request.getAmount()) < 0) {
            logger.warn("Insufficient balance for withdrawal: requested={}, available={}, wallet={}, user={}", 
                       request.getAmount(), wallet.getUsableBalance(), request.getWalletId(), userPrincipal.getUsername());
            throw new InsufficientBalanceException("Insufficient usable balance");
        }
        
        // Determine transaction status based on amount
        Transaction.TransactionStatus status = request.getAmount().compareTo(APPROVAL_THRESHOLD) >= 0 
                ? Transaction.TransactionStatus.PENDING 
                : Transaction.TransactionStatus.APPROVED;
        
        logger.debug("Withdrawal transaction status determined: {} for amount {}", status, request.getAmount());
        
        // Capture old balances for audit logging
        BigDecimal oldBalance = wallet.getBalance();
        BigDecimal oldUsableBalance = wallet.getUsableBalance();
        
        Transaction transaction = new Transaction(
                wallet,
                request.getAmount(),
                Transaction.TransactionType.WITHDRAW,
                request.getOppositePartyType(),
                request.getDestination(),
                status
        );
        
        transaction = transactionRepository.save(transaction);
        
        // Update wallet balances
        updateWalletBalancesForWithdraw(wallet, request.getAmount(), status);
        
        auditLogger.logTransactionCreation(transaction.getId(), "WITHDRAW", request.getAmount(), 
                                          request.getWalletId(), status.name(), userPrincipal.getUsername());
        
        auditLogger.logBalanceChange(wallet.getId(), oldBalance, wallet.getBalance(), 
                                   oldUsableBalance, wallet.getUsableBalance(), 
                                   "Withdrawal transaction", userPrincipal.getUsername());
        
        logger.info("Withdrawal transaction {} created successfully with status {} for wallet {}", 
                   transaction.getId(), status, request.getWalletId());
        
        return new TransactionResponse(transaction);
    }
    
    @Transactional(readOnly = true)
    public List<TransactionResponse> listTransactions(Long walletId, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        boolean isEmployee = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
        
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        
        // Check if user can access this wallet
        if (!isEmployee && !wallet.getCustomer().getId().equals(userPrincipal.getId())) {
            throw new UnauthorizedException("You can only view transactions for your own wallets");
        }
        
        List<Transaction> transactions = transactionRepository.findByWalletIdOrderByCreatedDateDesc(walletId);
        return transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public TransactionResponse approveTransaction(ApprovalRequest request, Authentication authentication) {
        // Only employees can approve transactions
        boolean isEmployee = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        if (!isEmployee) {
            logger.warn("Non-employee user {} attempted to approve transaction {}", 
                       userPrincipal.getUsername(), request.getTransactionId());
            auditLogger.logUnauthorizedAccess(userPrincipal.getUsername(), 
                                            "transaction", "approve transaction " + request.getTransactionId());
            throw new UnauthorizedException("Only employees can approve transactions");
        }
        
        logger.info("Processing transaction approval: transactionId={}, newStatus={}, employee={}", 
                   request.getTransactionId(), request.getStatus(), userPrincipal.getUsername());
        
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> {
                    logger.error("Transaction not found with ID: {}", request.getTransactionId());
                    return new ResourceNotFoundException("Transaction not found");
                });
        
        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            logger.warn("Attempted to modify non-pending transaction {} (current status: {}) by user {}", 
                       request.getTransactionId(), transaction.getStatus(), userPrincipal.getUsername());
            throw new IllegalStateException("Transaction is not in pending status");
        }
        
        Transaction.TransactionStatus oldStatus = transaction.getStatus();
        transaction.setStatus(request.getStatus());
        transaction.setProcessedDate(LocalDateTime.now());
        final Transaction finalTransaction = transactionRepository.save(transaction);
        
        // Update wallet balances based on approval decision
        Wallet wallet = walletRepository.findByIdForUpdate(finalTransaction.getWallet().getId()).orElseThrow();
        BigDecimal oldBalance = wallet.getBalance();
        BigDecimal oldUsableBalance = wallet.getUsableBalance();
        
        updateWalletBalancesForApproval(finalTransaction, request.getStatus(), wallet);
        
        auditLogger.logTransactionApproval(finalTransaction.getId(), oldStatus.name(), 
                                         request.getStatus().name(), finalTransaction.getAmount(), 
                                         userPrincipal.getUsername());
        
        auditLogger.logBalanceChange(wallet.getId(), oldBalance, wallet.getBalance(), 
                                   oldUsableBalance, wallet.getUsableBalance(), 
                                   "Transaction approval: " + request.getStatus(), userPrincipal.getUsername());
        
        logger.info("Transaction {} status changed from {} to {} by employee {}", 
                   finalTransaction.getId(), oldStatus, request.getStatus(), userPrincipal.getUsername());
        
        return new TransactionResponse(finalTransaction);
    }
    
    private void updateWalletBalancesForDeposit(Wallet wallet, BigDecimal amount, 
                                              Transaction.TransactionStatus status) {
        if (status == Transaction.TransactionStatus.APPROVED) {
            // Approved deposits update both balance and usable balance
            wallet.setBalance(wallet.getBalance().add(amount));
            wallet.setUsableBalance(wallet.getUsableBalance().add(amount));
        } else {
            // Pending deposits only update balance
            wallet.setBalance(wallet.getBalance().add(amount));
        }
        walletRepository.save(wallet);
    }
    
    private void updateWalletBalancesForWithdraw(Wallet wallet, BigDecimal amount, 
                                               Transaction.TransactionStatus status) {
        if (status == Transaction.TransactionStatus.APPROVED) {
            // Approved withdrawals update both balance and usable balance
            wallet.setBalance(wallet.getBalance().subtract(amount));
            wallet.setUsableBalance(wallet.getUsableBalance().subtract(amount));
        } else {
            // Pending withdrawals only update usable balance
            wallet.setUsableBalance(wallet.getUsableBalance().subtract(amount));
        }
        walletRepository.save(wallet);
    }
    
    private void updateWalletBalancesForApproval(Transaction transaction, 
                                               Transaction.TransactionStatus newStatus,
                                               Wallet wallet) {
        BigDecimal amount = transaction.getAmount();
        
        if (transaction.getType() == Transaction.TransactionType.DEPOSIT) {
            if (newStatus == Transaction.TransactionStatus.APPROVED) {
                // Pending deposit approved: add to usable balance
                wallet.setUsableBalance(wallet.getUsableBalance().add(amount));
            } else if (newStatus == Transaction.TransactionStatus.DENIED) {
                // Pending deposit denied: remove from balance
                wallet.setBalance(wallet.getBalance().subtract(amount));
            }
        } else { // WITHDRAW
            if (newStatus == Transaction.TransactionStatus.APPROVED) {
                // Pending withdraw approved: subtract from balance
                wallet.setBalance(wallet.getBalance().subtract(amount));
            } else if (newStatus == Transaction.TransactionStatus.DENIED) {
                // Pending withdraw denied: add back to usable balance
                wallet.setUsableBalance(wallet.getUsableBalance().add(amount));
            }
        }
        
        walletRepository.save(wallet);
    }
}
