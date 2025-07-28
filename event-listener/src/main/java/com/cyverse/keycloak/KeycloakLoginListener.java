package com.cyverse.keycloak;

import com.cyverse.keycloak.irods.service.IrodsService;
import com.cyverse.keycloak.ldap.service.LdapService;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
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
        // generic groups for all users
        ldapService.addLdapUserToGroup(user, "everyone");
        ldapService.addLdapUserToGroup(user, "community");
        // discovery-environment specific group
        ldapService.addLdapUserToGroup(user, "de-preview-access");
    }

    private void performIrodsActions(UserModel user) {
        // generic irods actions for all users
        irodsService.addIrodsUser(user);
        irodsService.grantIrodsUserAccess(user);
    }

    /**
     * Perform actions based on keycloak events.
     * Actions supported in this implementation: LDAP User creation, iRODS account
     * creation.
     *
     * @param event Event that triggers the actions
     */
    @Override
    public void onEvent(Event event) {
        if (!event.getType().equals(EventType.LOGIN)) {
            return;
        }

        RealmModel sessionRealm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(sessionRealm, event.getUserId());

        if (user != null && !sessionRealm.getName().contains("master")) {
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