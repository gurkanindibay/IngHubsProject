package com.ing.walletservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
@Table(name = "customers")
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;
    
    @NotBlank(message = "Surname is required")
    @Size(max = 100, message = "Surname must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String surname;
    
    @NotBlank(message = "TCKN is required")
    @Size(min = 11, max = 11, message = "TCKN must be exactly 11 digits")
    @Column(nullable = false, unique = true, length = 11)
    private String tckn;
    
    @NotBlank(message = "Username is required")
    @Column(nullable = false, unique = true)
    private String username;
    
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.CUSTOMER;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Wallet> wallets;
    
    // Constructors
    public Customer() {}
    
    public Customer(String name, String surname, String tckn, String username, String password, Role role) {
        this.name = name;
        this.surname = surname;
        this.tckn = tckn;
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSurname() {
        return surname;
    }
    
    public void setSurname(String surname) {
        this.surname = surname;
    }
    
    public String getTckn() {
        return tckn;
    }
    
    public void setTckn(String tckn) {
        this.tckn = tckn;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public List<Wallet> getWallets() {
        return wallets;
    }
    
    public void setWallets(List<Wallet> wallets) {
        this.wallets = wallets;
    }
    
    public enum Role {
        CUSTOMER, EMPLOYEE
    }
}
