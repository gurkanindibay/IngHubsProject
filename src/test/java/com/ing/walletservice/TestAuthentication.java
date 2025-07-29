package com.ing.walletservice;

import com.ing.walletservice.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class TestAuthentication implements Authentication {
    
    private final UserPrincipal principal;
    private boolean authenticated = true;
    
    public TestAuthentication(UserPrincipal principal) {
        this.principal = principal;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return principal.getAuthorities();
    }
    
    @Override
    public Object getCredentials() {
        return principal.getPassword();
    }
    
    @Override
    public Object getDetails() {
        return null;
    }
    
    @Override
    public Object getPrincipal() {
        return principal;
    }
    
    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    @Override
    public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
        this.authenticated = authenticated;
    }
    
    @Override
    public String getName() {
        return principal.getUsername();
    }
}
