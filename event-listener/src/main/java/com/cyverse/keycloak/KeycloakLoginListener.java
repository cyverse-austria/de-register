package com.cyverse.keycloak;

import com.cyverse.keycloak.irods.service.IrodsService;
import com.cyverse.keycloak.ldap.service.LdapService;
import com.cyverse.keycloak.portal.service.UserPortalService;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event-listener for LOGIN events.
 */
@Slf4j
public class KeycloakLoginListener implements EventListenerProvider {

    private static final Logger logger = Logger.getLogger(KeycloakLoginListener.class);

    private final KeycloakSession session;
    private final LdapService ldapService;
    private final IrodsService irodsService;
    private final UserPortalService userPortalService;
    private final List<String> clientIds;
    private final Map<String, Instant> minTimeBetweenLogins;

    public KeycloakLoginListener(KeycloakSession session,
                                 LdapService ldapService,
                                 IrodsService irodsService,
                                 UserPortalService userPortalService,
                                 List<String> clientIds) {
        this.session = session;
        this.ldapService = ldapService;
        this.irodsService = irodsService;
        this.userPortalService = userPortalService;
        this.clientIds = clientIds;
        this.minTimeBetweenLogins = new ConcurrentHashMap<>();
    }

    private void performLdapActions(UserModel user) {
        Map<String, String> ldapAttrs = ldapService.updateLdapUser(user);
        ldapAttrs.forEach(user::setSingleAttribute);
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
     * Helper method to avoid triggering consecutive API actions for the same LOGIN
     * event. Because some Keycloak authentication flows (e.g. IDP) can trigger for
     * one logical login multiple LOGIN events and for others not, it's hard to
     * differentiate between the variety of possible login events.
     * This algorithm should address this issue by temporarily storing a "red light"
     * time of 10s for a user in which no actions can be triggered (following the
     * supposition that the user does not try to authenticate 2 times in less than
     * 10s). After this time has passed permit actions again.
     * This helps avoid unnecessary traffic and cleanup the logs without the need of
     * additional persistence.
     */
    private boolean canTriggerActions(UserModel user) {
        Instant canTriggerTime = minTimeBetweenLogins.get(user.getUsername());
        Instant now = Instant.now();

        if (canTriggerTime == null || now.isAfter(canTriggerTime)) {
            Instant nextActionTrigger = Instant.now().plus(10, ChronoUnit.SECONDS);
            minTimeBetweenLogins.put(user.getUsername(), nextActionTrigger);
            return true;
        }

        logger.warn("Login event immediately after another.");
        return false;
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
        if (!event.getType().equals(EventType.LOGIN)
                || (!this.clientIds.isEmpty() && this.clientIds.stream()
                    .noneMatch(c -> c.equals(event.getClientId())))) {
            return;
        }

        logger.info("Event triggered login-listener");
        RealmModel sessionRealm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(sessionRealm, event.getUserId());

        if (user != null
                && !sessionRealm.getName().contains("master")
                && canTriggerActions(user)) {
            performLdapActions(user);
            performIrodsActions(user);
           // performUserPortalActions(user);
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