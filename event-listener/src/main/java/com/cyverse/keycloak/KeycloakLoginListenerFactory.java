package com.cyverse.keycloak;
import com.cyverse.keycloak.http.ListenerHttpClientBase;
import com.cyverse.keycloak.http.ListenerHttpClientWAuth;
import com.cyverse.keycloak.http.ListenerHttpClientWoAuth;
import com.cyverse.keycloak.http.TokenService;
import com.cyverse.keycloak.irods.service.IrodsService;
import com.cyverse.keycloak.irods.service.IrodsServiceImpl;
import com.cyverse.keycloak.irods.service.NoOpIrodsServiceImpl;
import com.cyverse.keycloak.ldap.service.LdapService;
import com.cyverse.keycloak.notification.service.NoOpNotificationServiceImpl;
import com.cyverse.keycloak.notification.service.NotificationService;
import com.cyverse.keycloak.notification.service.NotificationServiceImpl;
import com.cyverse.keycloak.portal.service.NoOpUserPortalServiceImpl;
import com.cyverse.keycloak.portal.service.UserPortalService;
import com.cyverse.keycloak.portal.service.UserPortalServiceImpl;
import com.cyverse.keycloak.ldap.service.LdapServiceImpl;
import com.cyverse.keycloak.ldap.service.NoOpLdapServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Factory for Keycloak event-listener.
 */
@Slf4j
public class KeycloakLoginListenerFactory implements EventListenerProviderFactory {
    private static final Logger logger = Logger.getLogger(KeycloakLoginListenerFactory.class);

    private LdapService ldapService;
    private IrodsService irodsService;
    private UserPortalService userPortalService;
    private NotificationService notificationService;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new KeycloakLoginListener(session, ldapService, irodsService, userPortalService, notificationService);
    }

    private boolean testConnection(ListenerHttpClientBase httpClient) {
        int retries = 5;
        while (retries-- != 0) {
            try {
                if (httpClient.testConnection()) {
                    return true;
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                logger.error("HTTP Client not responding. For this event listener will use no-op services.");
                return false;
            }
        }
        return false;
    }

    @Override
    public void init(Config.Scope config) {
        String host = config.get("api-service-host");

        String apiKey = config.get("api-key");
        String serviceMail = config.get("service-mail");
        String servicePassword = config.get("service-password");

        ListenerHttpClientBase httpClient;

        if (apiKey == null || apiKey.isEmpty()
                || serviceMail == null || serviceMail.isEmpty()
                || servicePassword == null || servicePassword.isEmpty()) {
            httpClient = new ListenerHttpClientWoAuth(host);
        } else {
            TokenService tokenService = new TokenService(serviceMail, servicePassword);
            httpClient = new ListenerHttpClientWAuth(host, apiKey, tokenService);
        }

        boolean connected = testConnection(httpClient);

        if (connected) {
            irodsService = new IrodsServiceImpl(httpClient);
            ldapService = new LdapServiceImpl(httpClient);
            userPortalService = new UserPortalServiceImpl(httpClient);
            notificationService = new NotificationServiceImpl(httpClient);
        } else {
            irodsService = new NoOpIrodsServiceImpl();
            ldapService = new NoOpLdapServiceImpl();
            userPortalService = new NoOpUserPortalServiceImpl();
            notificationService = new NoOpNotificationServiceImpl();
        }
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