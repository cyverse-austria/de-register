FROM debian:bullseye-slim

ENV DEBIAN_FRONTEND=noninteractive

ARG APP_VERSION
ENV APP_VERSION=${APP_VERSION}

# Install dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget \
    curl \
    ca-certificates \
    gnupg \
    lsb-release \
    bash \
    openjdk-17-jdk-headless && \
    rm -rf /var/lib/apt/lists/*

# Add iRODS repo key and repo
RUN wget -qO - https://packages.irods.org/irods-signing-key.asc | apt-key add - && \
    echo "deb [arch=amd64] https://packages.irods.org/apt $(lsb_release -sc) main" > /etc/apt/sources.list.d/irods.list

RUN apt-get update && apt-get install -y irods-runtime irods-icommands && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /root/.irods

COPY entrypoint.sh /entrypoint.sh

COPY target/api-service-${APP_VERSION}.jar /api-service.jar
COPY src/main/resources/api-service-config.yml /api-service-config.yml

RUN chmod +x /entrypoint.sh

EXPOSE 7000

ENTRYPOINT ["/entrypoint.sh"]
