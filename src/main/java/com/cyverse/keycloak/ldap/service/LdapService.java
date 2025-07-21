package com.cyverse.keycloak.ldap.service;

import com.cyverse.keycloak.ldap.config.LdapConfig;
import org.jboss.logging.Logger;
import org.keycloak.models.UserModel;
import org.keycloak.services.DefaultKeycloakSession;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Hashtable;
import java.util.Optional;

/**
 *  LDAP Service in Keycloak context.
 */
public class LdapService {
    private static final Logger logger = Logger.getLogger(LdapService.class);

    private final LdapConfig ldapConfig;
    private final Hashtable<String, String> env;

    public LdapService(LdapConfig ldapConfig) {
        this.ldapConfig = ldapConfig;
        this.env = new Hashtable<>();
    }

    public void init() {
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapConfig.getLdapHost());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, ldapConfig.getLdapAdmin());
        env.put(Context.SECURITY_CREDENTIALS, ldapConfig.getLdapPassword());
    }

    /**
     * Adds user to LDAP, if it does not already exist.
     *
     * @param user the UserModel that comes from Keycloak data-model
     */
    public void addLdapUser(UserModel user) {
        logger.debug("Try adding user to LDAP: " + user.getUsername());

        String entryDN = "uid=" + user.getUsername() +",ou=People," + ldapConfig.getLdapBaseDN();
        try {
            DirContext ctx = new InitialDirContext(env);

            ctx.createSubcontext(entryDN, getUserAttributes(ctx, user));
            logger.info("LDAP user added successfully: " + user.getUsername());

            ctx.close();
        } catch (NamingException e) {
            if (e.getMessage().contains("Entry Already Exists")) {
                logger.debug("User " + user.getUsername() + " already registered in LDAP");
                return;
            }
            logger.error(String.format("Error adding LDAP user: %s\n%s ", user.getUsername(), e.getMessage()));
        }
    }

    private Attributes getUserAttributes(DirContext ctx, UserModel user) {
        Attribute objClass = new BasicAttribute("objectClass");
        objClass.add("inetOrgPerson");
        objClass.add("posixAccount");
        //TODO set shadow properties?
        objClass.add("shadowAccount");

        Attributes attrs = new BasicAttributes(true);

        attrs.put(objClass);
        attrs.put("givenName", user.getFirstName());
        attrs.put("sn", user.getFirstName());
        attrs.put("cn", user.getFirstName() + " " + user.getLastName());
        attrs.put("uid", user.getUsername());
        attrs.put("mail", user.getEmail());
        //TODO custom exception with message for empty optional
        attrs.put("uidNumber", getLastAssignedUid(ctx).orElseThrow());
        attrs.put("gidNUmber", "10013");
        attrs.put("homeDirectory", "/home/" + user.getUsername());
        attrs.put("loginShell", "/bin/bash");

        return attrs;
    }

    /**
     * Get last LDAP UID assigned to a user.
     * Build a context search, perform it and get the maximum UID present in the configured LDAP
     * host.
     *
     * @return Optional UID number
     */
    private Optional<String> getLastAssignedUid(DirContext ctx) {
        String searchFilter = "(uidNumber=*)";

        try {

            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            searchControls.setReturningAttributes(new String[]{"uidNumber"});

            NamingEnumeration<SearchResult> results =
                    ctx.search(ldapConfig.getLdapBaseDN(), searchFilter, searchControls);

            long maxUid = 0;
            while (results.hasMore()) {
                SearchResult result = results.next();
                Attributes attrs = result.getAttributes();
                Attribute uidNumberAttr = attrs.get("uidNumber");
                long uidParsed = Long.parseLong((String) uidNumberAttr.get());
                if (uidParsed > maxUid) {
                    maxUid = uidParsed;
                }
            }

            return Optional.of(String.valueOf(++maxUid));
        } catch (NamingException e) {
            logger.error("Error searching uids: " + e.getMessage());
        }
        return Optional.empty();
    }
}
