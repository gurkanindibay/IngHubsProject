# ING Digital Wallet Service - API Testing Guide

A comprehensive guide for testing the Digital Wallet Service APIs with real-world scenarios and examples in LOCAL profile.

## 🎯 Testing Overview

This guide provides practical examples for testing all API endpoints, including authentication, wallet management, and transaction processing. Each example includes expected responses and common error scenarios.

Below includes tests using bash interface. If you want to test using GUI, swagger-ui is available at address `http://localhost:8080/swagger-ui/index.html` 

## 🔐 Authentication & Setup

### Step 1: Obtain JWT Token

#### Customer Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer1",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjdXN0b21lcjEi...",
    "id": 1,
    "username": "customer1",
    "role": "ROLE_CUSTOMER"
  }
}
```

#### Employee Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "employee1",
    "password": "password123"
  }'
```

### Step 2: Set Authentication Token
```bash
# Save the token for subsequent requests
export JWT_TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjdXN0b21lcjEi..."

# Or for Windows PowerShell
$JWT_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjdXN0b21lcjEi..."
```

## 💰 Wallet Management Operations

### Create a New Wallet
```bash
curl -X POST http://localhost:8080/api/wallets \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "walletName": "My USD Savings",
    "currency": "USD",
    "activeForShopping": true,
    "activeForWithdraw": true
  }'
```

**Expected Response:**
```json
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
    "activeForWithdraw": true,
    "customerId": 1
  }
}
```

### List Customer Wallets (as customer)
```bash
curl -X GET http://localhost:8080/api/wallets \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### List Specific Customer Wallets (as employee)
```bash
curl -X GET "http://localhost:8080/api/wallets?customerId=1" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Filter Wallets by Currency and Balance
```bash
curl -X GET "http://localhost:8080/api/wallets?currency=USD&minBalance=100" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Get Wallet Details
```bash
curl -X GET http://localhost:8080/api/wallets/1 \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Wallet retrieved successfully",
  "data": {
    "id": 1,
    "walletName": "My USD Savings",
    "currency": "USD",
    "balance": 500.00,
    "usableBalance": 450.00,
    "activeForShopping": true,
    "activeForWithdraw": true,
    "customerId": 1
  }
}
```

## 💸 Transaction Operations

### Small Deposit (Auto-approved)
```bash
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500.00,
    "walletId": 1,
    "source": "TR123456789012345678901234",
    "oppositePartyType": "IBAN"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Deposit processed successfully",
  "data": {
    "id": 1,
    "amount": 500.00,
    "type": "DEPOSIT",
    "status": "APPROVED",
    "oppositeParty": "TR123456789012345678901234",
    "oppositePartyType": "IBAN",
    "createdDate": "2025-07-26T18:30:00.000Z",
    "processedDate": "2025-07-26T18:30:00.000Z",
    "walletId": 1
  }
}
```

### Large Deposit (Requires approval)
```bash
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 2000.00,
    "walletId": 1,
    "source": "TR987654321098765432109876",
    "oppositePartyType": "IBAN"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Deposit submitted for approval",
  "data": {
    "id": 2,
    "amount": 2000.00,
    "type": "DEPOSIT",
    "status": "PENDING",
    "oppositeParty": "TR987654321098765432109876",
    "oppositePartyType": "IBAN",
    "createdDate": "2025-07-26T18:30:00.000Z",
    "processedDate": null,
    "walletId": 1
  }
}
```

### Withdrawal from Wallet
```bash
curl -X POST http://localhost:8080/api/transactions/withdraw \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 200.00,
    "walletId": 1,
    "destination": "TR111222333444555666777888",
    "oppositePartyType": "IBAN"
  }'
```

### Payment System Withdrawal
```bash
curl -X POST http://localhost:8080/api/transactions/withdraw \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 150.00,
    "walletId": 1,
    "destination": "PAY123456789",
    "oppositePartyType": "PAYMENT"
  }'
```

### List Wallet Transactions
```bash
curl -X GET http://localhost:8080/api/transactions/wallet/1 \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Transactions retrieved successfully",
  "data": [
    {
      "id": 1,
      "amount": 500.00,
      "type": "DEPOSIT",
      "status": "APPROVED",
      "oppositeParty": "TR123456789012345678901234",
      "oppositePartyType": "IBAN",
      "createdDate": "2025-07-26T18:30:00.000Z",
      "processedDate": "2025-07-26T18:30:00.000Z",
      "walletId": 1
    }
  ]
}
```

### Approve Transaction (Employee only)
```bash
curl -X POST http://localhost:8080/api/transactions/approve \
  -H "Authorization: Bearer $EMPLOYEE_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": 2,
    "status": "APPROVED"
  }'
```

### Deny Transaction (Employee only)
```bash
curl -X POST http://localhost:8080/api/transactions/approve \
  -H "Authorization: Bearer $EMPLOYEE_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": 3,
    "status": "DENIED"
  }'
```

## 🧪 Testing Scenarios & Workflows

### Complete Customer Journey
```bash
# 1. Customer logs in
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"customer1","password":"password123"}'

# 2. Create a USD wallet
curl -X POST http://localhost:8080/api/wallets \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"walletName":"My Savings","currency":"USD","activeForShopping":true,"activeForWithdraw":true}'

# 3. Make small deposit (auto-approved)
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":500,"walletId":1,"source":"TR123456789","oppositePartyType":"IBAN"}'

# 4. Make large deposit (pending approval)
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":2000,"walletId":1,"source":"TR987654321","oppositePartyType":"IBAN"}'

# 5. Try withdrawal exceeding usable balance (should fail)
curl -X POST http://localhost:8080/api/transactions/withdraw \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":600,"walletId":1,"destination":"TR111222333","oppositePartyType":"IBAN"}'

# 6. Valid withdrawal within usable balance
curl -X POST http://localhost:8080/api/transactions/withdraw \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":200,"walletId":1,"destination":"TR111222333","oppositePartyType":"IBAN"}'

# 7. Check transaction history
curl -X GET http://localhost:8080/api/transactions/wallet/1 \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Employee Approval Workflow
```bash
# 1. Employee logs in
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"employee1","password":"password123"}'

# 2. View customer wallets
curl -X GET "http://localhost:8080/api/wallets?customerId=1" \
  -H "Authorization: Bearer $EMPLOYEE_JWT_TOKEN"

# 3. View pending transactions
curl -X GET http://localhost:8080/api/transactions/wallet/1 \
  -H "Authorization: Bearer $EMPLOYEE_JWT_TOKEN"

# 4. Approve pending transaction
curl -X POST http://localhost:8080/api/transactions/approve \
  -H "Authorization: Bearer $EMPLOYEE_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"transactionId":2,"status":"APPROVED"}'

# 5. Verify balance update
curl -X GET http://localhost:8080/api/wallets/1 \
  -H "Authorization: Bearer $EMPLOYEE_JWT_TOKEN"
```

### Security & Authorization Testing
```bash
# 1. Try accessing another customer's wallet (should fail)
curl -X GET http://localhost:8080/api/wallets/2 \
  -H "Authorization: Bearer $CUSTOMER1_JWT_TOKEN"

# Expected: 403 Forbidden

# 2. Try transaction approval as customer (should fail)
curl -X POST http://localhost:8080/api/transactions/approve \
  -H "Authorization: Bearer $CUSTOMER1_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"transactionId":1,"status":"APPROVED"}'

# Expected: 403 Forbidden

# 3. Try API access without token (should fail)
curl -X GET http://localhost:8080/api/wallets

# Expected: 401 Unauthorized

# 4. Try API access with expired/invalid token (should fail)
curl -X GET http://localhost:8080/api/wallets \
  -H "Authorization: Bearer invalid_token_here"

# Expected: 401 Unauthorized
```

### Error Scenario Testing
```bash
# 1. Invalid currency
curl -X POST http://localhost:8080/api/wallets \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"walletName":"Test","currency":"GBP","activeForShopping":true,"activeForWithdraw":true}'

# Expected: 400 Bad Request

# 2. Negative deposit amount
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":-100,"walletId":1,"source":"TR123456789","oppositePartyType":"IBAN"}'

# Expected: 400 Bad Request

# 3. Withdrawal from inactive wallet
curl -X POST http://localhost:8080/api/transactions/withdraw \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":100,"walletId":999,"destination":"TR123456789","oppositePartyType":"IBAN"}'

# Expected: 404 Not Found

# 4. Insufficient balance withdrawal
curl -X POST http://localhost:8080/api/transactions/withdraw \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":10000,"walletId":1,"destination":"TR123456789","oppositePartyType":"IBAN"}'

# Expected: 400 Bad Request
```

## 📋 Response Formats & Status Codes

### Success Response Format
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    // Response payload
  }
}
```

### Error Response Format
```json
{
  "success": false,
  "message": "Detailed error description",
  "data": null
}
```

### Validation Error Response
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "walletName": "Wallet name cannot be empty",
    "currency": "Invalid currency code",
    "amount": "Amount must be positive"
  }
}
```

### HTTP Status Codes
| Code | Meaning | When Used |
|------|---------|-----------|
| **200** | OK | Successful GET, PUT operations |
| **201** | Created | Successful POST operations (wallet, transaction created) |
| **400** | Bad Request | Validation errors, business rule violations |
| **401** | Unauthorized | Missing or invalid JWT token |
| **403** | Forbidden | User lacks permission for operation |
| **404** | Not Found | Resource doesn't exist or not accessible |
| **500** | Internal Server Error | Unexpected server errors |

## 🎛 Business Rules Reference

### Transaction Amount Rules
- **< 1,000**: Automatically approved and processed
- **≥ 1,000**: Requires employee approval before processing
- **Minimum**: 0.01 (no zero or negative amounts)
- **Maximum**: No explicit limit (business decision)

### Currency Support
- **TRY**: Turkish Lira
- **USD**: US Dollar  
- **EUR**: Euro
- **Note**: Currency codes are case-sensitive and must be exactly 3 characters

### Balance Types Explained
- **Balance**: Total funds including pending deposits
- **Usable Balance**: Available funds excluding pending withdrawals
- **Example**: Balance=1000, Usable=800 means 200 is reserved for pending withdrawals

### Opposite Party Types
- **IBAN**: Bank account number (format: TR + 24 digits)
- **PAYMENT**: Payment system identifier (format: PAY + alphanumeric)

### Wallet Activity Flags
- **activeForShopping**: Allows spending/shopping transactions
- **activeForWithdraw**: Allows withdrawal transactions
- **Note**: Both flags are independent and can be set separately

## 🛠 Testing Tools & Tips

### Using Postman
1. **Import Environment**: Create environment variables for base_url and jwt_token
2. **Pre-request Scripts**: Automatically refresh JWT tokens
3. **Test Collections**: Group related API calls into collections
4. **Automated Testing**: Use Postman tests to validate responses

### Using Browser Developer Tools
1. **Network Tab**: Monitor actual API calls from frontend
2. **Console**: Test API calls directly with fetch() or axios
3. **Application Tab**: Inspect stored JWT tokens

### Command Line Testing
```bash
# Set base URL for easier testing
export BASE_URL="http://localhost:8080"

# Test API with pretty-printed JSON
curl -s "$BASE_URL/api/wallets" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq '.'

# Save response to file for analysis
curl -s "$BASE_URL/api/wallets" \
  -H "Authorization: Bearer $JWT_TOKEN" > wallets_response.json
```

### PowerShell Testing (Windows)
```powershell
# Set variables
$baseUrl = "http://localhost:8080"
$headers = @{'Authorization' = "Bearer $jwtToken"; 'Content-Type' = 'application/json'}

# Test API call
$response = Invoke-RestMethod -Uri "$baseUrl/api/wallets" -Headers $headers
$response | ConvertTo-Json -Depth 10
```

## 📊 Performance Testing

### Load Testing Example
```bash
# Simple load test with multiple concurrent requests
for i in {1..10}; do
  curl -X GET http://localhost:8080/api/wallets \
    -H "Authorization: Bearer $JWT_TOKEN" &
done
wait

# Monitor response times
time curl -X GET http://localhost:8080/api/wallets \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Concurrent Transaction Testing
```bash
# Test concurrent deposits to same wallet
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/transactions/deposit \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"amount\":10,\"walletId\":1,\"source\":\"TR$i\",\"oppositePartyType\":\"IBAN\"}" &
done
wait
```



