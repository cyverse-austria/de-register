package com.cyverse.keycloak;
import com.cyverse.keycloak.irods.config.IrodsConfig;
import com.cyverse.keycloak.irods.service.IrodsService;
import com.cyverse.keycloak.ldap.config.LdapConfig;
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

    private void initLdap(Config.Scope config) {
        LdapConfig ldapConfig = new LdapConfig(
                config.get("ldap-host"),
                config.get("ldap-admin"),
                config.get("ldap-password"),
                config.get("ldap-base-dn"),
                config.get("ldap-everyone-group")
        );
        ldapConfig.verifyFieldsSet();
        ldapService = new LdapService(ldapConfig);
        ldapService.init();
    }

    private void initIrods(Config.Scope config) {
        IrodsConfig irodsConfig = new IrodsConfig(config.get("irods-host"));
        irodsConfig.verifyFieldsSet();
        irodsService = new IrodsService(irodsConfig);
    }

    @Override
    public void init(Config.Scope config) {
        initLdap(config);
        initIrods(config);
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