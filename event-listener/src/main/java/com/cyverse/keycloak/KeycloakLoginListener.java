package com.cyverse.keycloak;

import com.cyverse.keycloak.irods.service.IrodsService;
import com.cyverse.keycloak.ldap.service.LdapService;
import com.cyverse.keycloak.portal.service.UserPortalService;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * Event-listener for LOGIN events.
 */
public class KeycloakLoginListener implements EventListenerProvider {

    private static final Logger logger = Logger.getLogger(KeycloakLoginListener.class);

    private final KeycloakSession session;
    private final LdapService ldapService;
    private final IrodsService irodsService;
    private final UserPortalService userPortalService;

    public KeycloakLoginListener(KeycloakSession session,
                                 LdapService ldapService,
                                 IrodsService irodsService,
                                 UserPortalService userPortalService) {
        this.session = session;
        this.ldapService = ldapService;
        this.irodsService = irodsService;
        this.userPortalService = userPortalService;
    }

    private void performLdapActions(UserModel user) {
        ldapService.updateLdapUser(user);
        // generic groups for all users
        ldapService.addLdapUserToGroup(user, "everyone");
        ldapService.addLdapUserToGroup(user, "community");

        // TODO add to discovery-environment specific group "de-preview-access" ?
        // see https://github.com/cyverse-de/portal2/blob/fcfecdfac381761d743fb4a312a6e779eec4397f/src/api/workflows/native/services.js#L23
    }

    private void performIrodsActions(UserModel user) {
        // generic irods actions for all users
        irodsService.addIrodsUser(user);
        irodsService.grantIrodsUserAccess(user);
    }

    private void performUserPortalActions(UserModel user) {
        userPortalService.addUserToPortal(user);
    }

    /**
     * Perform actions based on keycloak events.
     * Actions supported in this implementation: LDAP User update, iRODS account
     * creation, User Portal DB user creation.
     *
     * @param event Event that triggers the actions
     */
    @Override
    public void onEvent(Event event) {
        if (!event.getType().equals(EventType.LOGIN)) {
            return;
        }

        logger.info("Event triggered login-listener");
        RealmModel sessionRealm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(sessionRealm, event.getUserId());

        if (user != null && !sessionRealm.getName().contains("master")) {
            performLdapActions(user);
            performIrodsActions(user);
            performUserPortalActions(user);
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