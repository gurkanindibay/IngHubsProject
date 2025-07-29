package com.ing.walletservice.util;

import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 * Utility class for structured logging with MDC (Mapped Diagnostic Context)
 */
public class LoggingUtil {
    
    private static final String USER_ID = "userId";
    private static final String USERNAME = "username";
    private static final String REQUEST_ID = "requestId";
    private static final String OPERATION = "operation";
    private static final String CORRELATION_ID = "correlationId";
    
    /**
     * Set user context in MDC for logging
     */
    public static void setUserContext(Long userId, String username) {
        if (userId != null) {
            MDC.put(USER_ID, userId.toString());
        }
        if (username != null) {
            MDC.put(USERNAME, username);
        }
    }
    
    /**
     * Set operation context in MDC for logging
     */
    public static void setOperationContext(String operation) {
        if (operation != null) {
            MDC.put(OPERATION, operation);
        }
    }
    
    /**
     * Set request context in MDC for logging
     */
    public static void setRequestContext(String requestId, String correlationId) {
        if (requestId != null) {
            MDC.put(REQUEST_ID, requestId);
        }
        if (correlationId != null) {
            MDC.put(CORRELATION_ID, correlationId);
        }
    }
    
    /**
     * Clear all MDC context
     */
    public static void clearContext() {
        MDC.clear();
    }
    
    /**
     * Clear user context only
     */
    public static void clearUserContext() {
        MDC.remove(USER_ID);
        MDC.remove(USERNAME);
    }
    
    /**
     * Log performance metrics
     */
    public static void logPerformance(Logger logger, String operation, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        MDC.put("duration", String.valueOf(duration));
        logger.info("Performance metric: {} completed in {} ms", operation, duration);
        MDC.remove("duration");
    }
    
    /**
     * Log business metrics
     */
    public static void logBusinessMetric(Logger logger, String metric, Object value) {
        MDC.put("metricType", "business");
        MDC.put("metricName", metric);
        MDC.put("metricValue", String.valueOf(value));
        logger.info("Business metric: {} = {}", metric, value);
        MDC.remove("metricType");
        MDC.remove("metricName");
        MDC.remove("metricValue");
    }
}
