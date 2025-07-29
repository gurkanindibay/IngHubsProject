package com.ing.walletservice.security;

import com.ing.walletservice.entity.Customer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {
    
    private Long id;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    
    public UserPrincipal(Long id, String username, String password, 
                        Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }
    
    public static UserPrincipal create(Customer customer) {
        Collection<GrantedAuthority> authorities = Collections.singleton(
            new SimpleGrantedAuthority("ROLE_" + customer.getRole().name())
        );
        
        return new UserPrincipal(
            customer.getId(),
            customer.getUsername(),
            customer.getPassword(),
            authorities
        );
    }
    
    public Long getId() {
        return id;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}
