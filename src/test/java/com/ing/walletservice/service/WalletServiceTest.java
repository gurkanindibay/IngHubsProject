package com.ing.walletservice.service;

import com.ing.walletservice.TestAuthentication;
import com.ing.walletservice.audit.AuditLogger;
import com.ing.walletservice.dto.request.CreateWalletRequest;
import com.ing.walletservice.dto.response.WalletResponse;
import com.ing.walletservice.entity.Customer;
import com.ing.walletservice.entity.Wallet;
import com.ing.walletservice.exception.ResourceNotFoundException;
import com.ing.walletservice.exception.UnauthorizedException;
import com.ing.walletservice.repository.CustomerRepository;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private WalletService walletService;

    private AuditLogger auditLogger;

    private Customer customer;
    private Customer employee;
    private Wallet wallet;
    private UserPrincipal customerPrincipal;
    private UserPrincipal employeePrincipal;
    private Authentication customerAuthentication;
    private Authentication employeeAuthentication;

    @BeforeEach
    void setUp() {
        // Use reflection to set the AuditLogger since we can't mock it due to Java 24
        // compatibility issues
        auditLogger = new AuditLogger();

        try {
            java.lang.reflect.Field auditLoggerField = WalletService.class.getDeclaredField("auditLogger");
            auditLoggerField.setAccessible(true);
            auditLoggerField.set(walletService, auditLogger);
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
    }

    @Test
    void createWallet_WithValidRequest_ShouldCreateWallet() {
        // Given
        CreateWalletRequest request = new CreateWalletRequest("My New Wallet", Wallet.Currency.EUR, true, false);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // When
        WalletResponse response = walletService.createWallet(request, customerAuthentication);

        // Then
        assertNotNull(response);
        verify(customerRepository).findById(1L);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void createWallet_WithNonExistentCustomer_ShouldThrowResourceNotFoundException() {
        // Given
        CreateWalletRequest request = new CreateWalletRequest("My New Wallet", Wallet.Currency.EUR, true, false);

        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> walletService.createWallet(request, customerAuthentication));
    }

    @Test
    void listWallets_ForOwnWallets_ShouldReturnWallets() {
        // Given
        List<Wallet> wallets = Arrays.asList(wallet);

        when(walletRepository.findByCustomerIdWithFilters(1L, null, null)).thenReturn(wallets);

        // When
        List<WalletResponse> result = walletService.listWallets(null, null, null, customerAuthentication);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(walletRepository).findByCustomerIdWithFilters(1L, null, null);
    }

    @Test
    void listWallets_WithFilters_ShouldReturnFilteredWallets() {
        // Given
        List<Wallet> wallets = Arrays.asList(wallet);
        Wallet.Currency currency = Wallet.Currency.USD;
        BigDecimal minBalance = new BigDecimal("500.00");

        when(walletRepository.findByCustomerIdWithFilters(1L, currency, minBalance)).thenReturn(wallets);

        // When
        List<WalletResponse> result = walletService.listWallets(null, currency, minBalance, customerAuthentication);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(walletRepository).findByCustomerIdWithFilters(1L, currency, minBalance);
    }

    @Test
    void listWallets_CustomerTryingToAccessOtherCustomerWallets_ShouldThrowUnauthorizedException() {
        // When & Then
        assertThrows(UnauthorizedException.class,
                () -> walletService.listWallets(2L, null, null, customerAuthentication));
    }

    @Test
    void listWallets_ByEmployee_ShouldRequireCustomerId() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> walletService.listWallets(null, null, null, employeeAuthentication));
    }

    @Test
    void listWallets_ByEmployeeWithCustomerId_ShouldReturnWallets() {
        // Given
        List<Wallet> wallets = Arrays.asList(wallet);

        when(walletRepository.findByCustomerIdWithFilters(1L, null, null)).thenReturn(wallets);

        // When
        List<WalletResponse> result = walletService.listWallets(1L, null, null, employeeAuthentication);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(walletRepository).findByCustomerIdWithFilters(1L, null, null);
    }

    @Test
    void getWallet_ForOwnWallet_ShouldReturnWallet() {
        // Given
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        // When
        WalletResponse response = walletService.getWallet(1L, customerAuthentication);

        // Then
        assertNotNull(response);
        assertEquals(wallet.getId(), response.getId());
        verify(walletRepository).findById(1L);
    }

    @Test
    void getWallet_WithNonExistentWallet_ShouldThrowResourceNotFoundException() {
        // Given
        when(walletRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> walletService.getWallet(999L, customerAuthentication));
    }

    @Test
    void getWallet_ForOtherCustomerWallet_ShouldThrowUnauthorizedException() {
        // Given
        Customer otherCustomer = new Customer("Other", "Customer", "11122334455", "other", "password",
                Customer.Role.CUSTOMER);
        otherCustomer.setId(3L);
        Wallet otherWallet = new Wallet(otherCustomer, "Other Wallet", Wallet.Currency.USD, true, true);
        otherWallet.setId(1L);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(otherWallet));

        // When & Then
        assertThrows(UnauthorizedException.class,
                () -> walletService.getWallet(1L, customerAuthentication));
    }

    @Test
    void getWallet_ByEmployee_ShouldAllowAccessToAnyWallet() {
        // Given
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        // When
        WalletResponse response = walletService.getWallet(1L, employeeAuthentication);

        // Then
        assertNotNull(response);
        assertEquals(wallet.getId(), response.getId());
        verify(walletRepository).findById(1L);
    }

    @Test
    void createWallet_ShouldSetCorrectInitialBalances() {
        // Given
        CreateWalletRequest request = new CreateWalletRequest("Balance Test Wallet", Wallet.Currency.TRY, true, true);

        Wallet savedWallet = new Wallet(customer, request.getWalletName(), request.getCurrency(),
                request.getActiveForShopping(), request.getActiveForWithdraw());
        savedWallet.setId(2L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        // When
        WalletResponse response = walletService.createWallet(request, customerAuthentication);

        // Then
        assertNotNull(response);
        verify(walletRepository).save(argThat(wallet -> wallet.getBalance().equals(BigDecimal.ZERO) &&
                wallet.getUsableBalance().equals(BigDecimal.ZERO)));
    }

    @Test
    void createWallet_ByCustomer_ShouldNotSetCustomerId() {
        // Given
        CreateWalletRequest request = new CreateWalletRequest(2L, "Balance Test Wallet", Wallet.Currency.TRY, true,
                true);

        Wallet savedWallet = new Wallet(customer, request.getWalletName(), request.getCurrency(),
                request.getActiveForShopping(), request.getActiveForWithdraw());
        savedWallet.setId(2L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> walletService.createWallet(request, customerAuthentication));
    }

    @Test
    void createWallet_ShouldUseCorrectWalletSettings() {
        // Given
        CreateWalletRequest request = new CreateWalletRequest("Settings Test Wallet", Wallet.Currency.EUR, false, true);

        Wallet savedWallet = new Wallet(customer, request.getWalletName(), request.getCurrency(),
                request.getActiveForShopping(), request.getActiveForWithdraw());
        savedWallet.setId(3L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        // When
        WalletResponse response = walletService.createWallet(request, customerAuthentication);

        // Then
        assertNotNull(response);
        verify(walletRepository).save(argThat(wallet -> !wallet.getActiveForShopping() &&
                wallet.getActiveForWithdraw()));
    }

    @Test
    void listWallets_CustomerSpecifyingOwnId_ShouldWork() {
        // Given
        List<Wallet> wallets = Arrays.asList(wallet);

        when(walletRepository.findByCustomerIdWithFilters(1L, null, null)).thenReturn(wallets);

        // When
        List<WalletResponse> result = walletService.listWallets(1L, null, null, customerAuthentication);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(walletRepository).findByCustomerIdWithFilters(1L, null, null);
    }
}
