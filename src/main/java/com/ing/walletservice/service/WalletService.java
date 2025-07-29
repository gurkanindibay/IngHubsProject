package com.ing.walletservice.service;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AuditLogger auditLogger;

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public WalletResponse createWallet(CreateWalletRequest request, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        boolean isEmployee = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        long customerId = isEmployee ? request.getCustomerId() : userPrincipal.getId();
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    logger.error("Customer not found with ID: {}", customerId);
                    return new ResourceNotFoundException("Customer not found");
                });
        
        if (!isEmployee && request.getCustomerId() != null ) {
            logger.error("User {} attempted to create a wallet for customer ID {}",
                    userPrincipal.getUsername(), request.getCustomerId());
            throw new IllegalArgumentException("Invalid request: Customer Id should not be provided");
        }

        if (isEmployee && request.getCustomerId() == null) {
            logger.error("Employee {} attempted to create a wallet without specifying customer ID",
                    userPrincipal.getUsername());
            throw new IllegalArgumentException("Customer ID is required for employees");
        } 

        logger.info("Creating wallet for user: {}, wallet name: {}, currency: {}",
                userPrincipal.getUsername(), request.getWalletName(), request.getCurrency());

        Wallet wallet = new Wallet(
                customer,
                request.getWalletName(),
                request.getCurrency(),
                request.getActiveForShopping(),
                request.getActiveForWithdraw());

        wallet = walletRepository.save(wallet);

        auditLogger.logWalletCreation(customer.getId(), request.getWalletName(),
                request.getCurrency().name(), userPrincipal.getUsername());

        logger.info("Wallet created successfully with ID: {} for customer: {}",
                wallet.getId(), customer.getId());

        return new WalletResponse(wallet);
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> listWallets(Long customerId, Wallet.Currency currency,
            BigDecimal minBalance, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        boolean isEmployee = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        Long targetCustomerId = customerId;

        // If not employee, can only see own wallets
        if (!isEmployee) {
            targetCustomerId = userPrincipal.getId();
            if (customerId != null && !customerId.equals(userPrincipal.getId())) {
                logger.warn("User {} attempted to access wallets for customer {}",
                        userPrincipal.getUsername(), customerId);
                auditLogger.logUnauthorizedAccess(userPrincipal.getUsername(),
                        "wallets", "list for customer " + customerId);
                throw new UnauthorizedException("You can only view your own wallets");
            }
        }

        // If customerId not provided and user is employee, throw exception
        if (targetCustomerId == null && isEmployee) {
            logger.error("Employee {} attempted to list wallets without specifying customer ID",
                    userPrincipal.getUsername());
            throw new IllegalArgumentException("Customer ID is required for employees");
        }

        logger.debug("Listing wallets for customer: {}, currency: {}, minBalance: {}",
                targetCustomerId, currency, minBalance);

        List<Wallet> wallets = walletRepository.findByCustomerIdWithFilters(targetCustomerId, currency, minBalance);

        logger.info("Found {} wallets for customer: {}", wallets.size(), targetCustomerId);

        return wallets.stream()
                .map(WalletResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(Long walletId, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        boolean isEmployee = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        logger.debug("Retrieving wallet with ID: {} for user: {}", walletId, userPrincipal.getUsername());

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> {
                    logger.error("Wallet not found with ID: {}", walletId);
                    return new ResourceNotFoundException("Wallet not found");
                });

        // Check if user can access this wallet
        if (!isEmployee && !wallet.getCustomer().getId().equals(userPrincipal.getId())) {
            logger.warn("User {} attempted to access wallet {} owned by customer {}",
                    userPrincipal.getUsername(), walletId, wallet.getCustomer().getId());
            auditLogger.logUnauthorizedAccess(userPrincipal.getUsername(),
                    "wallet", "access wallet " + walletId);
            throw new UnauthorizedException("You can only access your own wallets");
        }

        logger.info("Wallet {} successfully retrieved for user {}", walletId, userPrincipal.getUsername());
        return new WalletResponse(wallet);
    }
}
