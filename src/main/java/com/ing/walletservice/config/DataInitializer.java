package com.ing.walletservice.config;

import com.ing.walletservice.entity.Customer;
import com.ing.walletservice.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing test data...");
        
        // Create test customer
        if (!customerRepository.existsByUsername("customer1")) {
            Customer customer = new Customer(
                "John",
                "Doe",
                "12345678901",
                "customer1",
                passwordEncoder.encode("password123"),
                Customer.Role.CUSTOMER
            );
            customerRepository.save(customer);
            logger.info("Created test customer: customer1");
        }
        
        // Create test employee
        if (!customerRepository.existsByUsername("employee1")) {
            Customer employee = new Customer(
                "Jane",
                "Smith",
                "12345678902",
                "employee1",
                passwordEncoder.encode("password123"),
                Customer.Role.EMPLOYEE
            );
            customerRepository.save(employee);
            logger.info("Created test employee: employee1");
        }
        
        // Create another test customer
        if (!customerRepository.existsByUsername("customer2")) {
            Customer customer2 = new Customer(
                "Alice",
                "Johnson",
                "12345678903",
                "customer2",
                passwordEncoder.encode("password123"),
                Customer.Role.CUSTOMER
            );
            customerRepository.save(customer2);
            logger.info("Created test customer: customer2");
        }
        
        logger.info("Test data initialization completed");
    }
}
