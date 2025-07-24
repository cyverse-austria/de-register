FROM bitnami/keycloak:23

COPY target/ /opt/bitnami/keycloak/providers/

ENTRYPOINT ["/opt/bitnami/keycloak/bin/kc.sh", "start-dev"]
