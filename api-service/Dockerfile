FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    wget \
    curl \
    ca-certificates \
    gnupg \
    lsb-release \
    bash \
    openjdk-17-jdk

# Add iRODS repo key and repo
RUN wget -qO - https://packages.irods.org/irods-signing-key.asc | apt-key add - && \
    echo "deb [arch=amd64] https://packages.irods.org/apt $(lsb_release -sc) main" > /etc/apt/sources.list.d/irods.list

RUN apt-get update && apt-get install -y irods-runtime irods-icommands && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /root/.irods

COPY entrypoint.sh /entrypoint.sh

COPY target/irods-client.jar /irods-client.jar
COPY src/main/resources/irods-client-config.yml /irods-client-config.yml

RUN chmod +x /entrypoint.sh

EXPOSE 7000

ENTRYPOINT ["/entrypoint.sh"]
