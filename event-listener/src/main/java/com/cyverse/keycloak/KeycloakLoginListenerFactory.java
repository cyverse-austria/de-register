package com.cyverse.keycloak;
import com.cyverse.keycloak.http.ListenerHttpClient;
import com.cyverse.keycloak.http.TokenService;
import com.cyverse.keycloak.irods.service.IrodsService;
import com.cyverse.keycloak.ldap.service.LdapService;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Factory for Keycloak event-listener.
 */
public class KeycloakLoginListenerFactory implements EventListenerProviderFactory {

    private LdapService ldapService;
    private IrodsService irodsService;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new KeycloakLoginListener(session, ldapService, irodsService);
    }

    private void testConnection(ListenerHttpClient httpClient) {
        int retries = 5;
        while (retries-- != 0) {
            try {
                if (httpClient.testConnection()) {
                    break;
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                throw new RuntimeException("HTTP Client not responding.");
            }
        }
    }

    @Override
    public void init(Config.Scope config) {
        String serviceMail = config.get("service-mail");
        String servicePassword = config.get("service-password");
        TokenService tokenService = new TokenService(serviceMail, servicePassword);

        String host = config.get("api-service-host");
        String apiKey = config.get("api-key");
        ListenerHttpClient httpClient = new ListenerHttpClient(host, apiKey, tokenService);
        testConnection(httpClient);

        irodsService = new IrodsService(httpClient);
        ldapService = new LdapService(httpClient);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "login-listener";
    }
}