package com.ing.walletservice.exception;

public class WalletNotActiveException extends RuntimeException {
    
    public WalletNotActiveException(String message) {
        super(message);
    }
    
    public WalletNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
