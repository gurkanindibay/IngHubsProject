FROM ubuntu:24.04

# Install Java 17 and Maven
RUN apt update && \
    apt install -y openjdk-17-jdk maven curl wget && \
    apt clean && \
    rm -rf /var/lib/apt/lists/*

# Set Java environment
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$PATH:$JAVA_HOME/bin

# Create app directory
WORKDIR /app

# Expose ports
EXPOSE 8080 8443

# Default command
CMD ["/bin/bash"]