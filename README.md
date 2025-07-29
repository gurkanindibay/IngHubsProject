# ING Digital Wallet Service

[![CI/CD Pipeline](https://github.com/gurkanindibay/IngCaseMyTryout/actions/workflows/ci.yml/badge.svg)](https://github.com/gurkanindibay/IngCaseMyTryout/actions/workflows/ci.yml)

A production-ready digital wallet management system built with Spring Boot, demonstrating enterprise-level architecture and best practices. This project showcases modern Java development techniques, security implementations, and scalable design patterns suitable for financial services.

## 🎯 Project Overview

This Digital Wallet Service is designed to demonstrate comprehensive understanding of:
- **Enterprise Java Development** with Spring Boot ecosystem
- **Financial Services Architecture** with proper transaction handling
- **Security-First Design** with JWT authentication and role-based access
- **Production-Ready Features** including logging, monitoring, and testing
- **Scalable Design Patterns** suitable for high-volume financial operations
- **Enterprise Evolution Strategy** with clear roadmap for microservices and advanced features

*For detailed enterprise enhancement plans, see [Further Improvements](src/docs/FURTHER_IMPROVEMENTS.md)*

## ✨ Key Features

### Core Business Functionality
- **Multi-Currency Wallet Management**: Support for TRY, USD, and EUR currencies
- **Intelligent Transaction Processing**: Automatic approval for small amounts, manual review for large transactions
- **Dual Balance System**: Separate tracking of total balance and usable balance for pending transactions
- **Role-Based Operations**: Customer self-service and employee administrative capabilities
- **Transaction History**: Complete audit trail with detailed transaction tracking

### Security & Compliance
- **JWT-Based Authentication**: Stateless, scalable authentication system
- **Role-Based Access Control**: Fine-grained permissions (Customer/Employee roles)
- **Data Protection**: BCrypt password encryption and secure data handling
- **Audit Logging**: Comprehensive financial transaction audit trail
- **Input Validation**: Bean Validation with custom business rule validation

### Technical Excellence
- **Production-Ready Architecture**: Clean separation of concerns with service layers
- **Enterprise Logging**: SLF4J with Logback, structured JSON audit logs
- **Comprehensive Testing**: Unit tests with high coverage (>50% line/branch coverage)
- **API Documentation**: OpenAPI 3.0 with Swagger UI
- **Exception Handling**: Global exception handling with meaningful error responses
- **Performance Monitoring**: Transaction timing and performance logging

## 🛠 Technology Stack

- **Framework**: Spring Boot 3.5.4
- **Java Version**: 17 (LTS)
- **Security**: Spring Security with JWT authentication
- **Database**: H2 (persistent file-based for development/demo)
- **ORM**: Spring Data JPA with Hibernate
- **API Documentation**: SpringDoc OpenAPI 3 (Swagger)
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Logging**: SLF4J with Logback (structured JSON logging)
- **Build Tool**: Maven 3.6+
- **Containerization**: Docker & Docker Compose
- **Web Server**: Embedded Tomcat with HTTPS/TLS support
- **Monitoring**: Spring Boot Actuator for health checks and metrics
- **Code Quality**: JaCoCo for test coverage analysis

## 📋 Business Logic & Rules

### Transaction Approval Workflow
- **Amounts < 1,000**: Automatically approved and processed
- **Amounts ≥ 1,000**: Require employee approval before processing
- **Smart Balance Management**: Separate usable balance calculation for pending transactions

### Balance Management System
- **Total Balance**: Includes all funds, including pending deposits
- **Usable Balance**: Available funds excluding pending withdrawals
- **Real-time Updates**: Immediate balance updates for approved transactions

### Access Control Matrix
| Role | Capabilities |
|------|-------------|
| **Customer** | Create wallets, view own data, deposit/withdraw from own wallets |
| **Employee** | View all customer data, approve/deny transactions, administrative operations |

### Supported Operations
- **Deposit Sources**: IBAN (bank transfers), PAYMENT (payment systems)
- **Withdrawal Destinations**: IBAN (bank transfers), PAYMENT (payment systems)
- **Currency Support**: TRY, USD, EUR with proper validation
- **Wallet Settings**: Independent shopping and withdrawal activation flags

## 🔄 Concurrency & Thread Safety

The Digital Wallet Service implements robust concurrency control mechanisms to ensure data consistency and prevent race conditions in high-volume financial operations. This is critical for maintaining accurate balances and preventing double-spending scenarios.

### Core Concurrency Strategy

#### 1. Pessimistic Database Locking
The system uses **pessimistic write locks** to prevent concurrent modifications to wallet balances:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT w FROM Wallet w WHERE w.id = :id")
Optional<Wallet> findByIdForUpdate(@Param("id") Long id);
```

**Key Benefits:**
- **Immediate Locking**: Locks wallet records at the database level during read
- **Race Condition Prevention**: Ensures only one transaction can modify a wallet at a time
- **Data Integrity**: Prevents lost updates and inconsistent balance calculations

#### 2. Transaction Isolation Levels
All financial operations use **READ_COMMITTED** isolation level:

```java
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public TransactionResponse deposit(DepositRequest request, Authentication authentication)
```

**Isolation Characteristics:**
- **Prevents Dirty Reads**: Transactions only see committed data
- **Allows Concurrent Reads**: Non-blocking read operations for better performance
- **Balanced Approach**: Optimal balance between consistency and performance


### Concurrency Testing & Validation

#### Comprehensive Test Coverage
The system includes dedicated concurrency tests that verify thread safety:

```java
@Test
void testConcurrentDeposits_withDistributedLocking() throws Exception {
    // Simulates 10 concurrent deposits to verify no race conditions
    int numberOfConcurrentDeposits = 10;
    BigDecimal depositAmount = new BigDecimal("10.00");
    // ... test implementation
}
```

**Test Scenarios:**
- **Concurrent Deposits**: Multiple simultaneous deposit operations
- **Concurrent Withdrawals**: Multiple withdrawal attempts with balance validation
- **Mixed Operations**: Combined deposits and withdrawals running simultaneously
- **Balance Verification**: Ensures final balances match expected calculations

#### Validation Results
✅ **10 concurrent deposits**: All processed correctly, final balance accurate  
✅ **5 concurrent withdrawals**: Proper balance deduction, no overdrafts  
✅ **Mixed operations**: Combined deposits/withdrawals maintain consistency  

## 🚀 Quick Start Guide

### Prerequisites
- Java 17 or higher ([Download OpenJDK](https://adoptium.net/))
- Maven 3.6+ ([Download Maven](https://maven.apache.org/download.cgi))
- Git (for cloning the repository)

### Option 1: Docker (Recommended - Single Command)
```bash
# Prerequisites: Docker and Docker Compose installed

# Clone the repository
git clone <repository-url>
cd wallet-service

# Start the service
docker compose up --build -d

# The service will be available at:
# 🌐 HTTPS: https://localhost:8443
# 🔗 HTTP:  http://localhost:8080 (redirects to HTTPS)
# ❤️ Health: https://localhost:8443/actuator/health
```

**Docker Features:**
- **Single Command Setup**: Complete environment with one command
- **Built-in HTTPS/TLS**: Spring Boot handles SSL termination directly
- **Health Monitoring**: Built-in health checks and monitoring
- **Persistent Data**: Docker volumes for database and logs
- **Simplified Architecture**: Direct SSL without proxy complexity

### Option 2: One-Command Setup (Windows)
Maven and Java > 17 should be installed on the computer
```bash
# Clone, build, and run in one step
git clone <repository-url>
cd wallet-service
mvn spring-boot:run
```

### Option 3: Manual Setup
```bash
# 1. Clone the repository
git clone <repository-url> <directory>
cd <directory>

# 2. Build the project
mvn clean install

# 3. Run the application
# Development (HTTP)
mvn spring-boot:run "-Dspring-boot.run.profiles=local"

# Production (HTTPS)
mvn spring-boot:run "-Dspring-boot.run.profiles=prod"

# Run tests during development
mvn test -"-Dspring-boot.run.profiles=local"

# 4Check code coverage
mvn clean test jacoco:report


# 4. Access the application
# Development: http://localhost:8080
# Production: https://localhost:8443 (HTTPS with self-signed cert)
# API docs: https://localhost:8443/swagger-ui/index.html
# Database: http://localhost:8080/h2-console (local profile only)
```

### HTTPS/TLS Configuration (Production)
The application is configured with SSL/TLS encryption for production security:

```bash
# Production access points (HTTPS enabled)
Main app: https://localhost:8443
API docs: https://localhost:8443/swagger-ui/index.html
Health: https://localhost:8443/actuator/health
HTTP redirect: http://localhost:8080 → https://localhost:8443

```

## 🐳 Docker Deployment

### Docker Compose Commands

```bash
# Start the service
docker compose up --build -d

# View logs
docker compose logs -f wallet-service

# Check service status
docker compose ps

# Stop services
docker compose down

# Clean up (remove volumes)
docker compose down -v

# Restart services
docker compose restart
```

### Docker Environment Options

The application supports flexible Docker deployment:

#### 1. Standard Deployment
```bash
# Start with default configuration
docker compose up --build -d

# Access: https://localhost:8443
```

### Health Monitoring

Docker containers include built-in health checks:

```bash
# Check container health
docker compose ps

# View health status
curl -k https://localhost:8443/actuator/health

# Monitor logs
docker compose logs -f wallet-service
```


## 📚 Extra Documentation

This project includes comprehensive documentation for different audiences:

- **[API Testing Guide](src/docs/API_TESTING.md)** - Complete API examples and testing scenarios
- **[Security Summary](src/docs/SECURITY_SUMMARY.md)** - Security architecture and configuration
- **[Logging Framework](src/docs/LOGGING.md)** - Enterprise logging setup and best practices
- **[Further Improvements](src/docs/FURTHER_IMPROVEMENTS.md)** - Enterprise evolution roadmap and advanced features

## 🔐 Authentication & Security

### JWT Authentication Flow
```bash
# 1. Login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"customer1","password":"password123"}'

# Response includes JWT token
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "id": 1,
    "username": "customer1",
    "role": "ROLE_CUSTOMER"
  }
}

# 2. Use token for authenticated requests
curl -X GET http://localhost:8080/api/wallets \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Test Users (Pre-configured)
| Username | Password | Role | Use Case |
|----------|----------|------|----------|
| `customer1` | `password123` | CUSTOMER | Customer operations, wallet management |
| `customer2` | `password123` | CUSTOMER | Multi-user testing scenarios |
| `employee1` | `password123` | EMPLOYEE | Administrative operations, approvals |

## 🏗 Architecture & Design

### Layered Architecture
```
┌─────────────────┐
│   Controllers   │ ← REST API endpoints, request validation
├─────────────────┤
│    Services     │ ← Business logic, transaction management
├─────────────────┤
│  Repositories   │ ← Data access layer, JPA queries
├─────────────────┤
│    Database     │ ← H2 persistent storage
└─────────────────┘
```

### Core Entities
- **Customer**: User accounts with authentication
- **Wallet**: Multi-currency digital wallets
- **Transaction**: Deposit/withdrawal operations with approval workflow


*See [Further Improvements](src/docs/FURTHER_IMPROVEMENTS.md) for comprehensive enterprise enhancement plans*

## 🌐 API Reference

### Authentication Endpoints
```http
POST /api/auth/login          # Authenticate user and get JWT token
```

### Wallet Management
```http
GET    /api/wallets           # List user's wallets (customers) or all wallets (employees)
POST   /api/wallets           # Create new wallet
GET    /api/wallets/{id}      # Get wallet details
```

### Transaction Operations
```http
POST   /api/transactions/deposit     # Deposit funds to wallet
POST   /api/transactions/withdraw    # Withdraw funds from wallet
GET    /api/transactions/wallet/{id} # List wallet transactions
POST   /api/transactions/approve     # Approve/deny transaction (employee only)
```

### Example Request/Response
```bash
# Create wallet
POST /api/wallets
{
  "walletName": "My USD Savings",
  "currency": "USD",
  "activeForShopping": true,
  "activeForWithdraw": true
}

# Response
{
  "success": true,
  "message": "Wallet created successfully",
  "data": {
    "id": 1,
    "walletName": "My USD Savings",
    "currency": "USD",
    "balance": 0.00,
    "usableBalance": 0.00,
    "activeForShopping": true,
    "activeForWithdraw": true
  }
}
```

## 💾 Database & Persistence

### H2 Database Configuration
- **Storage Type**: File-based persistent storage
- **Location**: `./data/walletdb.mv.db`
- **Console Access**: http://localhost:8080/h2-console (local profile only)
- **Credentials**: username=`sa`, password=`password`
- **Persistence**: Data survives application restarts

### Database Schema
```sql
-- Core entities with proper relationships
CUSTOMERS (id, name, surname, tckn, username, password, role)
WALLETS (id, customer_id, wallet_name, currency, balance, usable_balance, active_for_shopping, active_for_withdraw)
TRANSACTIONS (id, wallet_id, amount, type, opposite_party, opposite_party_type, status, created_date, processed_date)
```

### Data Initialization
- Pre-configured test users and sample data
- Automatic schema creation and updates
- Development-friendly data persistence


### Environment Variables
```bash
# JWT Configuration
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000

# Database Configuration (for production)
DATABASE_URL=jdbc:postgresql://localhost:5432/walletdb
DATABASE_USERNAME=wallet_user
DATABASE_PASSWORD=secure_password

# CORS Configuration
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
```

## 🚨 Error Handling & Validation

### HTTP Status Codes
- **200 OK**: Successful operations
- **201 Created**: Resource created successfully
- **400 Bad Request**: Validation errors, business rule violations
- **401 Unauthorized**: Invalid or missing authentication
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Unexpected server errors

### Error Response Format
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "walletName": "Wallet name cannot be empty",
    "currency": "Invalid currency code"
  }
}
```



## 📊 Monitoring & Logging

### Comprehensive Logging System
- **Application Logs**: `logs/wallet-service.log`
- **Error Logs**: `logs/wallet-service-error.log`
- **Audit Logs**: `logs/wallet-service-audit.log` (JSON format)


## 🚀 Production Considerations

### Scalability Features
- Stateless JWT authentication
- Database connection pooling ready
- Async logging capabilities
- Horizontal scaling support

### Security Hardening
- BCrypt password encryption
- JWT token expiration
- CORS protection
- Input sanitization
- SQL injection prevention

### Performance Optimizations
- Efficient database queries
- Connection pooling
- Caching headers
- Log level optimization
- Resource cleanup

### Deployment Checklist
- [ ] Environment variables configured
- [ ] Database migrations applied
- [ ] Security certificates installed
- [ ] Monitoring systems connected
- [ ] Backup procedures tested
- [ ] Load balancer configured

---

