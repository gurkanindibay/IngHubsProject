# Digital Wallet Service - Security Architecture Summary

A comprehensive overview of the production-ready security features implemented in the Digital Wallet Service, designed for financial services compliance and enterprise deployment.

## 🛡️ Security Overview

### Security Principles
- **Authentication First**: Every API endpoint requires valid JWT authentication
- **Authorization Always**: Role-based access control for all operations
- **Input Validation**: Comprehensive validation at multiple layers
- **Audit Everything**: Complete audit trail for all financial operations
- **Fail Securely**: Graceful degradation with secure defaults

## 🔐 Authentication & Authorization

### JWT-Based Authentication
```
🎯 Stateless Design: No server-side sessions
🔄 Token Rotation: 24-hour expiration (configurable)
🔒 Secure Algorithms: HMAC SHA-256 signing
📱 Mobile-Friendly: Perfect for web and mobile clients
```

#### Token Structure
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "customer1",
    "role": "ROLE_CUSTOMER",
    "iat": 1627849200,
    "exp": 1627935600
  }
}
```

### Role-Based Access Control (RBAC)

| Role | Access Level | Capabilities |
|------|-------------|--------------|
| **CUSTOMER** | Personal Data Only | Own wallets, own transactions, deposit/withdraw |
| **EMPLOYEE** | All Customer Data | View all wallets, approve transactions, admin operations |

#### Access Control Matrix
```
Resource          | Customer | Employee
------------------|----------|----------
Own Wallets       |    ✅    |    ✅
Other Wallets     |    ❌    |    ✅
Own Transactions  |    ✅    |    ✅
All Transactions  |    ❌    |    ✅
Approve/Deny      |    ❌    |    ✅
```

## 🌐 CORS & Cross-Origin Security

### Production CORS Configuration
```yaml
Allowed Origins: https://test.inghubcase.com
Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
Allowed Headers: Authorization, Content-Type, Accept
Credentials Support: Enabled
Max Age: 3600 seconds
```

### Environment-Specific CORS
```bash
# Local Development (CORS Disabled)
curl -X GET http://localhost:8080/api/wallets \
  -H "Origin: http://localhost:3000" \
  -H "Authorization: Bearer $JWT_TOKEN"
# ✅ Allowed - All origins permitted in local mode

# Production (CORS Enforced)
curl -X GET http://localhost:8080/api/wallets \
  -H "Origin: https://malicious-site.com" \
  -H "Authorization: Bearer $JWT_TOKEN"
# ❌ Blocked - Origin not in whitelist
```

## 🔒 Data Protection & Encryption

### Password Security
- **Algorithm**: BCrypt with configurable strength (default: 10 rounds)
- **Salt**: Automatically generated unique salt per password
- **Storage**: Only hashed passwords stored, never plaintext
- **Validation**: Server-side validation prevents weak passwords

```java
// Example password hashing (implementation detail)
String hashedPassword = passwordEncoder.encode("plainTextPassword");
boolean matches = passwordEncoder.matches("plainTextPassword", hashedPassword);
```

### Sensitive Data Handling
```
✅ JWT Secrets: Configurable via environment variables
✅ Database Credentials: External configuration support
✅ API Keys: Never logged or exposed in responses
✅ Personal Data: Minimal exposure in logs and responses
```

## 🛂 Input Validation & Sanitization

### Bean Validation (JSR-303)
```java
// Example validation annotations
public class WalletRequest {
    @NotBlank(message = "Wallet name is required")
    @Size(max = 100, message = "Wallet name too long")
    private String walletName;
    
    @NotNull
    @Pattern(regexp = "^(TRY|USD|EUR)$", message = "Invalid currency")
    private String currency;
    
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @DecimalMax(value = "999999.99", message = "Amount too large")
    private BigDecimal amount;
}
```

### SQL Injection Prevention
- **JPA/Hibernate**: Parameterized queries by default
- **No Native SQL**: All queries use JPA Query Language
- **Input Sanitization**: Automatic escaping of special characters

### XSS Prevention
- **Content-Type Headers**: Proper JSON content type enforcement
- **Input Encoding**: Automatic encoding of user inputs
- **Output Escaping**: Safe rendering of user-generated content

## 📊 Audit & Monitoring

### Comprehensive Audit Trail
```json
{
  "timestamp": "2025-07-26T18:30:00.000Z",
  "level": "INFO",
  "logger": "com.ing.walletservice.audit",
  "message": "Transaction created: DEPOSIT 500.00 for wallet 1 with status APPROVED by user customer1",
  "mdc": {
    "eventType": "TRANSACTION_CREATED",
    "username": "customer1",
    "walletId": "1",
    "amount": "500.00",
    "transactionType": "DEPOSIT",
    "status": "APPROVED"
  }
}
```

### Security Event Logging
- **Authentication Events**: Login success/failure, token validation
- **Authorization Failures**: Unauthorized access attempts
- **Business Rule Violations**: Invalid transactions, balance issues
- **System Events**: Application startup, shutdown, configuration changes

### Monitoring & Alerting Ready
```
🔍 Structured Logs: JSON format for easy parsing
📈 Metrics Ready: Performance and security metrics
🚨 Alert Points: Authentication failures, unauthorized access
📊 Dashboards: Ready for ELK Stack, Splunk, or Grafana
```

## 🔧 Configuration Security

### Environment-Based Configuration
```bash
# Production Environment Variables
export JWT_SECRET="your-256-bit-secret-key-here"
export DATABASE_URL="jdbc:postgresql://prod-db:5432/walletdb"
export DATABASE_USERNAME="wallet_service_user"
export DATABASE_PASSWORD="secure-database-password"
export CORS_ALLOWED_ORIGINS="https://wallet.yourcompany.com"
export LOG_LEVEL="INFO"
```

### Profile-Based Security
```properties
# application-prod.properties
spring.h2.console.enabled=false
spring.jpa.show-sql=false
logging.level.com.ing.walletservice=INFO
app.cors.allowed-origins=https://test.inghubcase.com

# application-local.properties  
spring.h2.console.enabled=true
spring.jpa.show-sql=true
logging.level.com.ing.walletservice=DEBUG
app.cors.enabled=false
```

## 🚀 Quick Setup & Testing

### Development Environment
```bash
# Start with relaxed security for development
export SPRING_PROFILES_ACTIVE=local
mvn spring-boot:run

# Access points
📍 Application: http://localhost:8080
📚 API Docs: http://localhost:8080/swagger-ui/index.html
🗄️ H2 Console: http://localhost:8080/h2-console (enabled)
🔍 Health: http://localhost:8080/actuator/health
```

### Production Environment
```bash
# Start with full security enabled (HTTPS)
mvn spring-boot:run  # Uses 'prod' profile by default

# Access points (HTTPS enabled)
📍 Application: https://localhost:8443
📚 API Docs: https://localhost:8443/swagger-ui/index.html
🗄️ H2 Console: ❌ Disabled for security
🔍 Health: https://localhost:8443/actuator/health
🔄 HTTP Redirect: http://localhost:8080 → https://localhost:8443
```


### Compliance Features
```
✅ Financial Transaction Logging: Every money movement recorded
✅ User Action Tracking: Complete audit trail for all operations
✅ Access Control Logging: Failed access attempts recorded
✅ Data Protection: No sensitive data in logs (passwords, tokens)
✅ Retention Policy: Configurable log retention and archival
```


## 🎯 Production Deployment Security

### Security Checklist
```
☐ Environment variables configured (JWT_SECRET, DB credentials)
☐ CORS origins restricted to production domains
☐ H2 console disabled in production
☑️ HTTPS/TLS configured with valid certificates
☐ SSL keystore deployed with production certificates
☐ HTTP to HTTPS redirect enabled
☐ Security headers configured
```

## 🏆 Security Best Practices Implemented

### 1. **Defense in Depth**
- Multiple security layers: authentication, authorization, validation, logging
- No single point of failure in security architecture

### 2. **Principle of Least Privilege**
- Users only access resources they own or are authorized to manage
- Employees have elevated privileges only for necessary operations

### 3. **Secure by Default**
- All endpoints require authentication unless explicitly public
- Conservative CORS and security settings

### 4. **Auditability & Transparency**
- Every financial operation logged with context
- Security events tracked for forensic analysis
- Comprehensive monitoring for suspicious activities

---

**🔒 Security Summary**: This Digital Wallet Service implements enterprise-grade security suitable for financial services, with comprehensive protection against common threats, robust audit capabilities, and compliance-ready features. The multi-profile configuration ensures both developer productivity and production security.
