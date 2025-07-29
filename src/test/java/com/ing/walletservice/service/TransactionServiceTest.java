package com.ing.walletservice.service;

import com.ing.walletservice.TestAuthentication;
import com.ing.walletservice.audit.AuditLogger;
import com.ing.walletservice.dto.request.ApprovalRequest;
import com.ing.walletservice.dto.request.DepositRequest;
import com.ing.walletservice.dto.request.WithdrawRequest;
import com.ing.walletservice.dto.response.TransactionResponse;
import com.ing.walletservice.entity.Customer;
import com.ing.walletservice.entity.Transaction;
import com.ing.walletservice.entity.Wallet;
import com.ing.walletservice.exception.InsufficientBalanceException;
import com.ing.walletservice.exception.ResourceNotFoundException;
import com.ing.walletservice.exception.UnauthorizedException;
import com.ing.walletservice.exception.WalletNotActiveException;
import com.ing.walletservice.repository.TransactionRepository;
import com.ing.walletservice.repository.WalletRepository;
import com.ing.walletservice.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;
    

    @InjectMocks
    private TransactionService transactionService;
    
    private AuditLogger auditLogger;

    private Customer customer;
    private Customer employee;
    private Wallet wallet;
    private UserPrincipal customerPrincipal;
    private UserPrincipal employeePrincipal;
    private Transaction transaction;
    private Authentication customerAuthentication;
    private Authentication employeeAuthentication;

    @BeforeEach
    void setUp() {
        // Use reflection to set the AuditLogger since we can't mock it due to Java 24 compatibility issues
        auditLogger = new AuditLogger();
        
        try {
            java.lang.reflect.Field auditLoggerField = TransactionService.class.getDeclaredField("auditLogger");
            auditLoggerField.setAccessible(true);
            auditLoggerField.set(transactionService, auditLogger);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject AuditLogger", e);
        }
        
        
        // Create test customer
        customer = new Customer("John", "Doe", "12345678901", "customer1", "password", Customer.Role.CUSTOMER);
        customer.setId(1L);

        // Create test employee
        employee = new Customer("Jane", "Smith", "98765432101", "employee1", "password", Customer.Role.EMPLOYEE);
        employee.setId(2L);

        // Create test wallet
        wallet = new Wallet(customer, "Test Wallet", Wallet.Currency.USD, true, true);
        wallet.setId(1L);
        wallet.setBalance(new BigDecimal("1000.00"));
        wallet.setUsableBalance(new BigDecimal("1000.00"));

        // Create UserPrincipals
        Collection<GrantedAuthority> customerAuthorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        customerPrincipal = new UserPrincipal(1L, "customer1", "password", customerAuthorities);

        Collection<GrantedAuthority> employeeAuthorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
        employeePrincipal = new UserPrincipal(2L, "employee1", "password", employeeAuthorities);
        
        // Create concrete Authentication instances
        customerAuthentication = new TestAuthentication(customerPrincipal);
        employeeAuthentication = new TestAuthentication(employeePrincipal);

        // Create test transaction
        transaction = new Transaction(wallet, new BigDecimal("500.00"), Transaction.TransactionType.DEPOSIT,
                Transaction.OppositePartyType.IBAN, "TR123456789", Transaction.TransactionStatus.PENDING);
        transaction.setId(1L);
    }

    @Test
    void deposit_WithSmallAmount_ShouldCreateApprovedTransaction() {
        // Given
        DepositRequest request = new DepositRequest(new BigDecimal("500.00"), 1L, "TR123456789", 
                Transaction.OppositePartyType.IBAN);
        
        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        TransactionResponse response = transactionService.deposit(request, customerAuthentication);

        // Then
        assertNotNull(response);
        verify(walletRepository).findByIdForUpdate(1L);
        verify(transactionRepository).save(any(Transaction.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void deposit_WithLargeAmount_ShouldCreatePendingTransaction() {
        // Given
        DepositRequest request = new DepositRequest(new BigDecimal("15000.00"), 1L, "TR123456789", 
                Transaction.OppositePartyType.IBAN);
        
        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        TransactionResponse response = transactionService.deposit(request, customerAuthentication);

        // Then
        assertNotNull(response);
        verify(walletRepository).findByIdForUpdate(1L);
        verify(transactionRepository).save(any(Transaction.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void deposit_WithExactThresholdAmount_ShouldCreatePendingTransaction() {
        // Given - exactly 1000 should trigger PENDING status
        DepositRequest request = new DepositRequest(new BigDecimal("1000.00"), 1L, "TR123456789", 
                Transaction.OppositePartyType.IBAN);
        
        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        TransactionResponse response = transactionService.deposit(request, customerAuthentication);

        // Then
        assertNotNull(response);
        verify(walletRepository).findByIdForUpdate(1L);
        verify(transactionRepository).save(any(Transaction.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void deposit_WithBelowThresholdAmount_ShouldCreateApprovedTransaction() {
        // Given - 999.99 should trigger APPROVED status
        DepositRequest request = new DepositRequest(new BigDecimal("999.99"), 1L, "TR123456789", 
                Transaction.OppositePartyType.IBAN);
        
        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        TransactionResponse response = transactionService.deposit(request, customerAuthentication);

        // Then
        assertNotNull(response);
        verify(walletRepository).findByIdForUpdate(1L);
        verify(transactionRepository).save(any(Transaction.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void deposit_WithNonExistentWallet_ShouldThrowResourceNotFoundException() {
        // Given
        DepositRequest request = new DepositRequest(new BigDecimal("500.00"), 999L, "TR123456789", 
                Transaction.OppositePartyType.IBAN);
        
        when(walletRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
                () -> transactionService.deposit(request, customerAuthentication));
    }

    @Test
    void deposit_ToOtherCustomerWallet_ShouldThrowUnauthorizedException() {
        // Given
        Customer otherCustomer = new Customer("Other", "Customer", "11122334455", "other", "password", Customer.Role.CUSTOMER);
        otherCustomer.setId(3L);
        Wallet otherWallet = new Wallet(otherCustomer, "Other Wallet", Wallet.Currency.USD, true, true);
        otherWallet.setId(2L);
        
        DepositRequest request = new DepositRequest(new BigDecimal("500.00"), 2L, "TR123456789", 
                Transaction.OppositePartyType.IBAN);
        
        when(walletRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(otherWallet));

        // When & Then
        assertThrows(UnauthorizedException.class, 
                () -> transactionService.deposit(request, customerAuthentication));
    }

    @Test
    void deposit_ByEmployee_ShouldAllowAccessToAnyWallet() {
        // Given
        DepositRequest request = new DepositRequest(new BigDecimal("500.00"), 1L, "TR123456789", 
                Transaction.OppositePartyType.IBAN);
        
        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        TransactionResponse response = transactionService.deposit(request, employeeAuthentication);

        // Then
        assertNotNull(response);
        verify(walletRepository).findByIdForUpdate(1L);
        verify(transactionRepository).save(any(Transaction.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void withdraw_WithSufficientBalance_ShouldCreateTransaction() {
        // Given
        WithdrawRequest request = new WithdrawRequest(new BigDecimal("500.00"), 1L, "TR123456789", 
                Transaction.OppositePartyType.IBAN);
        
        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        TransactionResponse response = transactionService.withdraw(request, customerAuthentication);

        // Then
        assertNotNull(response);
        verify(walletRepository).findByIdForUpdate(1L);
        verify(transactionRepository).save(any(Transaction.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void withdraw_WithLargeAmount_ShouldCreatePendingTransaction() {
        // Given
        wallet.setUsableBalance(new BigDecimal("2000.00")); // Ensure sufficient balance
        WithdrawRequest request = new WithdrawRequest(new BigDecimal("1500.00"), 1L, "TR123456789", 
                Transaction.OppositePartyType.IBAN);
        
        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        TransactionResponse response = transactionService.withdraw(request, customerAuthentication);

        // Then
        assertNotNull(response);
        verify(walletRepository).findByIdForUpdate(1L);
        verify(transactionRepository).save(any(Transaction.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void withdraw_WithExactThresholdAmount_ShouldCreatePendingTransaction() {
        // Given - exactly 1000 should trigger PENDING status
        wallet.setUsableBalance(new BigDecimal("1500.00")); // Ensure sufficient balance
        WithdrawRequest request = new WithdrawRequest(new BigDecimal("1000.00"), 1L, "TR123456789", 
                Transaction.OppositePartyType.IBAN);
        
        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        TransactionResponse response = transactionService.withdraw(request, customerAuthentication);

        // Then
        assertNotNull(response);
        verify(walletRepository).findByIdForUpdate(1L);
        verify(transactionRepository).save(any(Transaction.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void withdraw_WithBelowThresholdAmount_ShouldCreateApprovedTransaction() {
        // Given - 999.99 should trigger APPROVED status
        WithdrawRequest request = new WithdrawRequest(new BigDecimal("999.99"), 1L, "TR123456789", 
                Transaction.OppositePartyType.IBAN);
        
        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        TransactionResponse response = transactionService.withdraw(request, customerAuthentication);

        // Then
        assertNotNull(response);
        verify(walletRepository).findByIdForUpdate(1L);
        verify(transactionRepository).save(any(Transaction.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void withdraw_WithInsufficientBalance_ShouldThrowInsufficientBalanceException() {
        // Given
        WithdrawRequest request = new WithdrawRequest(new BigDecimal("2000.00"), 1L, "TR123456789", 
                Transaction.OppositePartyType.IBAN);
        
        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));

        // When & Then
        assertThrows(InsufficientBalanceException.class, 
                () -> transactionService.withdraw(request, customerAuthentication));
    }

    @Test
    void withdraw_WithNonExistentWallet_ShouldThrowResourceNotFoundException() {
        // Given
        WithdrawRequest request = new WithdrawRequest(new BigDecimal("500.00"), 999L, "TR123456789", 
                Transaction.OppositePartyType.IBAN);
        
        when(walletRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
                () -> transactionService.withdraw(request, customerAuthentication));
    }

    @Test
    void withdraw_ToOtherCustomerWallet_ShouldThrowUnauthorizedException() {
        // Given
        Customer otherCustomer = new Customer("Other", "Customer", "11122334455", "other", "password", Customer.Role.CUSTOMER);
        otherCustomer.setId(3L);
        Wallet otherWallet = new Wallet(otherCustomer, "Other Wallet", Wallet.Currency.USD, true, true);
        
        WithdrawRequest request = new WithdrawRequest(new BigDecimal("500.00"), 2L, "TR123456789", 
                Transaction.OppositePartyType.IBAN);
        
        when(walletRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(otherWallet));

        // When & Then
        assertThrows(UnauthorizedException.class, 
                () -> transactionService.withdraw(request, customerAuthentication));
    }

    @Test
    void withdraw_FromInactiveWallet_ShouldThrowWalletNotActiveException() {
        // Given
        Wallet inactiveWallet = new Wallet(customer, "Inactive Wallet", Wallet.Currency.USD, false, false);
        inactiveWallet.setId(2L);
        inactiveWallet.setBalance(new BigDecimal("1000.00"));
        inactiveWallet.setUsableBalance(new BigDecimal("1000.00"));
        
        WithdrawRequest request = new WithdrawRequest(new BigDecimal("500.00"), 2L, "TR123456789", 
                Transaction.OppositePartyType.IBAN);
        
        when(walletRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(inactiveWallet));

        // When & Then
        assertThrows(WalletNotActiveException.class, 
                () -> transactionService.withdraw(request, customerAuthentication));
    }

    @Test
    void listTransactions_ForOwnWallet_ShouldReturnTransactions() {
        // Given
        List<Transaction> transactions = Arrays.asList(transaction);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletIdOrderByCreatedDateDesc(1L)).thenReturn(transactions);

        // When
        List<TransactionResponse> result = transactionService.listTransactions(1L, customerAuthentication);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(walletRepository).findById(1L); // Only called once for listTransactions (no distributed lock)
        verify(transactionRepository).findByWalletIdOrderByCreatedDateDesc(1L);
    }

    @Test
    void listTransactions_ForOtherCustomerWallet_ShouldThrowUnauthorizedException() {
        // Given
        Customer otherCustomer = new Customer("Other", "Customer", "11122334455", "other", "password", Customer.Role.CUSTOMER);
        otherCustomer.setId(3L);
        Wallet otherWallet = new Wallet(otherCustomer, "Other Wallet", Wallet.Currency.USD, true, true);
        otherWallet.setId(2L);
        
        when(walletRepository.findById(2L)).thenReturn(Optional.of(otherWallet));

        // When & Then
        assertThrows(UnauthorizedException.class, 
                () -> transactionService.listTransactions(2L, customerAuthentication));
    }

    @Test
    void listTransactions_WithNonExistentWallet_ShouldThrowResourceNotFoundException() {
        // Given
        when(walletRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
                () -> transactionService.listTransactions(999L, customerAuthentication));
    }

    @Test
    void approveTransaction_ApproveDeposit_ShouldUpdateBalancesCorrectly() {
        // Given
        ApprovalRequest request = new ApprovalRequest(1L, Transaction.TransactionStatus.APPROVED);
        
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // When
        TransactionResponse response = transactionService.approveTransaction(request, employeeAuthentication);

        // Then
        assertNotNull(response);
        verify(transactionRepository).findById(1L);
        verify(transactionRepository).save(any(Transaction.class));
        verify(walletRepository).findByIdForUpdate(1L);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void approveTransaction_DenyDeposit_ShouldUpdateBalancesCorrectly() {
        // Given
        ApprovalRequest request = new ApprovalRequest(1L, Transaction.TransactionStatus.DENIED);
        
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // When
        TransactionResponse response = transactionService.approveTransaction(request, employeeAuthentication);

        // Then
        assertNotNull(response);
        verify(transactionRepository).findById(1L);
        verify(transactionRepository).save(any(Transaction.class));
        verify(walletRepository).findByIdForUpdate(1L);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void approveTransaction_ApproveWithdraw_ShouldUpdateBalancesCorrectly() {
        // Given
        Transaction withdrawTransaction = new Transaction(wallet, new BigDecimal("500.00"), 
                Transaction.TransactionType.WITHDRAW, Transaction.OppositePartyType.IBAN, 
                "TR123456789", Transaction.TransactionStatus.PENDING);
        
        ApprovalRequest request = new ApprovalRequest(1L, Transaction.TransactionStatus.APPROVED);
        
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(withdrawTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(withdrawTransaction);
        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // When
        TransactionResponse response = transactionService.approveTransaction(request, employeeAuthentication);

        // Then
        assertNotNull(response);
        verify(transactionRepository).findById(1L);
        verify(transactionRepository).save(any(Transaction.class));
        verify(walletRepository).findByIdForUpdate(1L);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void approveTransaction_DenyWithdraw_ShouldUpdateBalancesCorrectly() {
        // Given
        Transaction withdrawTransaction = new Transaction(wallet, new BigDecimal("500.00"), 
                Transaction.TransactionType.WITHDRAW, Transaction.OppositePartyType.IBAN, 
                "TR123456789", Transaction.TransactionStatus.PENDING);
        
        ApprovalRequest request = new ApprovalRequest(1L, Transaction.TransactionStatus.DENIED);
        
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(withdrawTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(withdrawTransaction);
        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // When
        TransactionResponse response = transactionService.approveTransaction(request, employeeAuthentication);

        // Then
        assertNotNull(response);
        verify(transactionRepository).findById(1L);
        verify(transactionRepository).save(any(Transaction.class));
        verify(walletRepository).findByIdForUpdate(1L);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void approveTransaction_WithNonExistentTransaction_ShouldThrowResourceNotFoundException() {
        // Given
        ApprovalRequest request = new ApprovalRequest(999L, Transaction.TransactionStatus.APPROVED);
        
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
                () -> transactionService.approveTransaction(request, employeeAuthentication));
    }

    @Test
    void approveTransaction_WithNonPendingTransaction_ShouldThrowIllegalStateException() {
        // Given
        Transaction approvedTransaction = new Transaction(wallet, new BigDecimal("500.00"), 
                Transaction.TransactionType.DEPOSIT, Transaction.OppositePartyType.IBAN, 
                "TR123456789", Transaction.TransactionStatus.APPROVED);
        
        ApprovalRequest request = new ApprovalRequest(1L, Transaction.TransactionStatus.APPROVED);
        
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(approvedTransaction));

        // When & Then
        assertThrows(IllegalStateException.class, 
                () -> transactionService.approveTransaction(request, employeeAuthentication));
    }

    @Test
    void approveTransaction_ByCustomer_ShouldThrowUnauthorizedException() {
        // Given
        ApprovalRequest request = new ApprovalRequest(1L, Transaction.TransactionStatus.APPROVED);

        // When & Then
        assertThrows(UnauthorizedException.class, 
                () -> transactionService.approveTransaction(request, customerAuthentication));
    }
}
