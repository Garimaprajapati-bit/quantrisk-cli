FROM openjdk:17-jdk-slim

# Install dependencies
RUN apt-get update && apt-get install -y \
    curl \
    jq \
    bc \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy JAR file
COPY target/quantrisk.jar .

# Copy sample portfolio for testing
COPY portfolio.csv .

# Create script for easy execution
RUN echo '#!/bin/bash\njava -jar quantrisk.jar "$@"' > /usr/local/bin/quantrisk && \
    chmod +x /usr/local/bin/quantrisk

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD java -jar quantrisk.jar --portfolio portfolio.csv > /dev/null || exit 1

ENTRYPOINT ["java", "-jar", "quantrisk.jar"]
