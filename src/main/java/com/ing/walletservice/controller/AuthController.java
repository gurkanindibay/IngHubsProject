package com.ing.walletservice.controller;

import com.ing.walletservice.audit.AuditLogger;
import com.ing.walletservice.dto.request.LoginRequest;
import com.ing.walletservice.dto.response.ApiResponse;
import com.ing.walletservice.dto.response.LoginResponse;
import com.ing.walletservice.security.JwtUtils;
import com.ing.walletservice.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuditLogger auditLogger;
    
    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, AuditLogger auditLogger) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.auditLogger = auditLogger;
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Authentication attempt for user: {}", loginRequest.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String role = userPrincipal.getAuthorities().iterator().next().getAuthority();
            
            LoginResponse response = new LoginResponse(jwt, userPrincipal.getId(), 
                                                     userPrincipal.getUsername(), role);
            
            auditLogger.logAuthenticationSuccess(userPrincipal.getUsername(), role);
            logger.info("User {} successfully authenticated with role {}", 
                       userPrincipal.getUsername(), role);
            
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
            
        } catch (BadCredentialsException e) {
            auditLogger.logAuthenticationFailure(loginRequest.getUsername(), "Invalid credentials");
            logger.warn("Authentication failed for user {}: Invalid credentials", loginRequest.getUsername());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid username or password"));
        } catch (AuthenticationException e) {
            auditLogger.logAuthenticationFailure(loginRequest.getUsername(), e.getMessage());
            logger.error("Authentication failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Authentication failed"));
        } catch (Exception e) {
            logger.error("Unexpected error during authentication for user {}: {}", 
                        loginRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("An unexpected error occurred"));
        }
    }
}
