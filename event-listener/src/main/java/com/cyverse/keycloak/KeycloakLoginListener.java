package com.cyverse.keycloak;

import com.cyverse.keycloak.irods.service.IrodsService;
import com.cyverse.keycloak.ldap.service.LdapService;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import java.util.Objects;

/**
 * Event-listener for LOGIN events.
 */
public class KeycloakLoginListener implements EventListenerProvider {

    private final KeycloakSession session;
    private final LdapService ldapService;
    private final IrodsService irodsService;

    public KeycloakLoginListener(KeycloakSession session, LdapService ldapService, IrodsService irodsService) {
        this.session = session;
        this.ldapService = ldapService;
        this.irodsService = irodsService;
    }

    private void performLdapActions(UserModel user) {
        ldapService.addLdapUser(user);
    }

    private void performIrodsActions(UserModel user) {
        irodsService.addIrodsUser(user);
    }

    /**
     * Perform actions based on keycloak events.
     * Actions supported in this implementation: LDAP User creation, iRODS account
     * creation.
     * @param event Event that triggers the actions
     */
    @Override
    public void onEvent(Event event) {
        if (!event.getType().equals(EventType.LOGIN)) {
            return;
        }
        UserModel user = session.users().getUserById(session.getContext().getRealm(), event.getUserId());

        if (user != null && !Objects.equals(user.getUsername(), "admin")) {
            performLdapActions(user);
            performIrodsActions(user);
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        // nothing to do for now
    }

    @Override
    public void close() {
    }

}