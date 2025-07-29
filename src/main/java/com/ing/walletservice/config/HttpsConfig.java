package com.ing.walletservice.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * HTTPS Configuration for Production Environment
 * 
 * This configuration enables HTTPS/TLS encryption for secure communication
 * in production deployments. It includes:
 * - SSL/TLS certificate configuration
 * - HTTP to HTTPS redirect
 * - Security constraints for financial data protection
 */
@Configuration
@Profile("prod")
public class HttpsConfig {

    @Value("${server.port:8443}")
    private int httpsPort;

    @Value("${server.http.port:8080}")
    private int httpPort;

    /**
     * Configure Tomcat to redirect HTTP requests to HTTPS
     * This ensures all traffic is encrypted for financial data security
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        
        // Add HTTP connector that redirects to HTTPS
        tomcat.addAdditionalTomcatConnectors(redirectConnector());
        return tomcat;
    }

    /**
     * HTTP Connector that redirects all HTTP traffic to HTTPS
     * Ensures no financial data is transmitted over unencrypted connections
     */
    private Connector redirectConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(httpPort);
        connector.setSecure(false);
        connector.setRedirectPort(httpsPort);
        return connector;
    }
}
