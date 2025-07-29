package com.ing.walletservice.service;

import com.ing.walletservice.entity.Customer;
import com.ing.walletservice.entity.Transaction;
import com.ing.walletservice.entity.Wallet;
import com.ing.walletservice.dto.request.DepositRequest;
import com.ing.walletservice.dto.request.WithdrawRequest;
import com.ing.walletservice.repository.CustomerRepository;
import com.ing.walletservice.repository.TransactionRepository;
import com.ing.walletservice.repository.WalletRepository;
import com.ing.walletservice.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for distributed locking in TransactionService
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
public class ConcurrentTransactionsTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Customer customer;
    private Wallet wallet;
    private Authentication customerAuth;

    @BeforeEach
    void setUp() {
        // Clean up previous test data
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        customerRepository.deleteAll();

        // Create test customer
        customer = new Customer("Test", "User", "99999999999", "testuser123", "password", Customer.Role.CUSTOMER);
        customer = customerRepository.save(customer);

        // Create test wallet
        wallet = new Wallet(customer, "Test Wallet", Wallet.Currency.USD, true, true);
        wallet.setBalance(new BigDecimal("100.00"));
        wallet.setUsableBalance(new BigDecimal("100.00"));
        wallet = walletRepository.save(wallet);
        
        System.out.println("Setup: Created wallet with ID: " + wallet.getId() + ", balance: " + wallet.getBalance());

        // Create authentication
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        UserPrincipal userPrincipal = new UserPrincipal(customer.getId(), customer.getUsername(), 
                                                        "password", authorities);
        customerAuth = new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void testConcurrentDeposits_withDistributedLocking() throws Exception {
        // This test verifies that concurrent deposits don't create race conditions
        int numberOfConcurrentDeposits = 10;
        BigDecimal depositAmount = new BigDecimal("10.00");
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfConcurrentDeposits);
        
        ExecutorService executor = Executors.newFixedThreadPool(numberOfConcurrentDeposits);
        
        // Submit concurrent deposit requests
        for (int i = 0; i < numberOfConcurrentDeposits; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    
                    DepositRequest request = new DepositRequest();
                    request.setWalletId(wallet.getId());
                    request.setAmount(depositAmount);
                    request.setOppositePartyType(Transaction.OppositePartyType.IBAN);
                    request.setSource("Test Bank");
                    
                    transactionService.deposit(request, customerAuth);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for all threads to complete
        doneLatch.await();
        executor.shutdown();
        
        // Verify the final balance
        Wallet finalWallet = walletRepository.findById(wallet.getId()).orElseThrow();
        BigDecimal expectedBalance = new BigDecimal("100.00").add(depositAmount.multiply(new BigDecimal(numberOfConcurrentDeposits)));
        
        assertEquals(expectedBalance, finalWallet.getBalance());
        assertEquals(expectedBalance, finalWallet.getUsableBalance());
    }

    @Test
    void testConcurrentWithdrawals_withDistributedLocking() throws Exception {
        // This test verifies that concurrent withdrawals don't create race conditions
        int numberOfConcurrentWithdrawals = 5;
        BigDecimal withdrawAmount = new BigDecimal("10.00");
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfConcurrentWithdrawals);
        
        ExecutorService executor = Executors.newFixedThreadPool(numberOfConcurrentWithdrawals);
        
        // Submit concurrent withdrawal requests
        for (int i = 0; i < numberOfConcurrentWithdrawals; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    
                    WithdrawRequest request = new WithdrawRequest();
                    request.setWalletId(wallet.getId());
                    request.setAmount(withdrawAmount);
                    request.setOppositePartyType(Transaction.OppositePartyType.IBAN);
                    request.setDestination("Test Bank Account");
                    
                    transactionService.withdraw(request, customerAuth);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for all threads to complete
        doneLatch.await();
        executor.shutdown();
        
        // Verify the final balance
        Wallet finalWallet = walletRepository.findById(wallet.getId()).orElseThrow();
        BigDecimal expectedBalance = new BigDecimal("100.00").subtract(withdrawAmount.multiply(new BigDecimal(numberOfConcurrentWithdrawals)));
        
        assertEquals(expectedBalance, finalWallet.getBalance());
        assertEquals(expectedBalance, finalWallet.getUsableBalance());
    }

    @Test
    void testMixedConcurrentOperations_withDistributedLocking() throws Exception {
        // This test verifies mixed concurrent operations (deposits and withdrawals)
        int numberOfDeposits = 5;
        int numberOfWithdrawals = 3;
        BigDecimal operationAmount = new BigDecimal("5.00");
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfDeposits + numberOfWithdrawals);
        
        ExecutorService executor = Executors.newFixedThreadPool(numberOfDeposits + numberOfWithdrawals);
        
        // Submit deposit requests
        for (int i = 0; i < numberOfDeposits; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    DepositRequest request = new DepositRequest();
                    request.setWalletId(wallet.getId());
                    request.setAmount(operationAmount);
                    request.setOppositePartyType(Transaction.OppositePartyType.IBAN);
                    request.setSource("Test Bank");
                    
                    transactionService.deposit(request, customerAuth);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        // Submit withdrawal requests
        for (int i = 0; i < numberOfWithdrawals; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    WithdrawRequest request = new WithdrawRequest();
                    request.setWalletId(wallet.getId());
                    request.setAmount(operationAmount);
                    request.setOppositePartyType(Transaction.OppositePartyType.IBAN);
                    request.setDestination("Test Bank Account");
                    
                    transactionService.withdraw(request, customerAuth);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for all threads to complete
        doneLatch.await();
        executor.shutdown();
        
        // Verify the final balance
        Wallet finalWallet = walletRepository.findById(wallet.getId()).orElseThrow();
        BigDecimal depositTotal = operationAmount.multiply(new BigDecimal(numberOfDeposits));
        BigDecimal withdrawTotal = operationAmount.multiply(new BigDecimal(numberOfWithdrawals));
        BigDecimal expectedBalance = new BigDecimal("100.00").add(depositTotal).subtract(withdrawTotal);
        
        assertEquals(expectedBalance, finalWallet.getBalance());
        assertEquals(expectedBalance, finalWallet.getUsableBalance());
    }
}
