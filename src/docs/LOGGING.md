# Logging Framework Documentation

## Overview

The Digital Wallet Service uses **SLF4J (Simple Logging Facade for Java) with Logback** as the logging framework. This is the industry standard for Java applications and provides excellent performance, flexibility, and features.

## Why SLF4J + Logback?

### Advantages:
1. **Performance**: Logback is faster than other logging frameworks
2. **Zero-dependency**: No additional jars required in Spring Boot
3. **Structured Logging**: Support for JSON format and MDC (Mapped Diagnostic Context)
4. **Flexible Configuration**: XML-based configuration with conditionals
5. **Rolling File Appenders**: Automatic log rotation and compression
6. **Industry Standard**: Widely adopted and well-documented
7. **Spring Boot Default**: No additional configuration needed

### Features Used:
- **Multiple Appenders**: Console, file, error-specific, and audit logs
- **Log Rotation**: Automatic file rotation by size and date
- **JSON Structured Logging**: For audit logs using Logstash encoder
- **MDC Context**: Request/user context in all log messages
- **Performance Logging**: Method execution time tracking
- **Audit Trail**: Complete financial transaction logging

## Log Configuration

### Log Levels
- **ERROR**: System errors, exceptions
- **WARN**: Business rule violations, security warnings
- **INFO**: Business events, transaction completions
- **DEBUG**: Detailed execution flow (development only)

### Log Files
All logs are stored in the `logs/` directory:

1. **wallet-service.log**: Main application log (all levels)
2. **wallet-service-error.log**: Error logs only
3. **wallet-service-audit.log**: Audit trail (JSON format)

### Log Rotation
- **Max File Size**: 10MB per file
- **Retention**: 30 days for main logs, 90 days for audit logs
- **Compression**: Automatic gzip compression of rotated files
- **Total Size Cap**: 100MB for main logs, 200MB for audit logs

## Usage Guidelines

### Log Method Purposes

#### Basic Logging Methods
- **logger.info()**: Record significant business events, transaction completions, and system status changes
- **logger.debug()**: Capture detailed execution flow and variable states for development troubleshooting
- **logger.warn()**: Alert about potential issues, business rule violations, and recoverable problems
- **logger.error()**: Document system errors, exceptions, and unrecoverable failures

#### Structured Logging with MDC
- **Purpose**: Add contextual information (user ID, transaction ID, session ID) that appears in all subsequent log messages within the same thread
- **Usage**: Set context at the beginning of request processing, clear at the end

#### Audit Logging
- **Purpose**: Create immutable records of financial transactions and security-sensitive operations for compliance and forensic analysis
- **Usage**: Log all money movements, authentication events, and administrative actions

#### Performance Logging
- **Purpose**: Monitor execution times of critical operations to identify performance bottlenecks and system health
- **Usage**: Measure and log duration of database operations, external API calls, and business-critical processes

## Log Format

### Console/File Format
```
2025-01-15 10:30:45.123 [http-nio-8080-exec-1] INFO  c.i.w.service.WalletService - Creating wallet for user: customer1, wallet name: My Wallet, currency: USD
```

### JSON Audit Format
```json
{
  "timestamp": "2025-01-15T10:30:45.123Z",
  "level": "INFO",
  "logger": "com.ing.walletservice.audit",
  "message": "Wallet created: My Wallet for customer 1 by user customer1",
  "mdc": {
    "eventType": "WALLET_CREATION",
    "customerId": "1",
    "walletName": "My Wallet",
    "currency": "USD",
    "username": "customer1"
  }
}
```

## Security Considerations

### Data Protection
- **No Passwords**: Never log passwords or sensitive authentication data
- **No PII**: Avoid logging personal identifiable information
- **No Financial Details**: Log transaction IDs, not account numbers
- **Token Safety**: Never log JWT tokens or API keys

### Audit Requirements
- **Financial Transactions**: All money movements are logged
- **Authentication Events**: Login attempts, failures, successes
- **Authorization Failures**: Unauthorized access attempts
- **Administrative Actions**: Employee actions on customer data

## Monitoring Integration

### Log Aggregation
The JSON audit logs are compatible with:
- **ELK Stack** (Elasticsearch, Logstash, Kibana)
- **Splunk**
- **Fluentd**
- **Grafana Loki**

### Alerts
Set up alerts for:
- **ERROR level logs**: System failures
- **WARN level security events**: Unauthorized access attempts
- **Business metrics**: High-value transactions
- **Performance issues**: Slow response times

## Configuration

### Environment-Specific Configuration
For different environments, modify `logback-spring.xml`:

#### Development
```xml
<root level="DEBUG">
    <appender-ref ref="CONSOLE"/>
</root>
```

#### Production
```xml
<root level="INFO">
    <appender-ref ref="FILE"/>
    <appender-ref ref="ERROR_FILE"/>
</root>
```

