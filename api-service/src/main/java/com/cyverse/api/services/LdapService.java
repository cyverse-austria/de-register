package com.cyverse.api.services;

import com.cyverse.api.config.LdapServiceConfig;
import com.cyverse.api.exceptions.ResourceAlreadyExistsException;
import com.cyverse.api.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Hashtable;
import java.util.Optional;

public class LdapService {
    private static final Logger logger = LoggerFactory.getLogger(LdapService.class);
    private final LdapServiceConfig ldapConfig;
    private final Hashtable<String, String> env;

    public LdapService(LdapServiceConfig config) {
        this.ldapConfig = config;
        this.env = new Hashtable<>();
    }

    /**
     * Init the LDAP environment.
     */
    public void init() {
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapConfig.getHost());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, ldapConfig.getAdmin());
        env.put(Context.SECURITY_CREDENTIALS, ldapConfig.getPassword());
    }

    /**
     * Adds user to LDAP, if it does not already exist.
     *
     * @param user the user to register in LDAP
     */
    public void addLdapUser(UserModel user) throws ResourceAlreadyExistsException {
        logger.debug("Try adding user to LDAP: {}", user.getUsername());

        String entryDN = "uid=" + user.getUsername() +",ou=People," + ldapConfig.getBaseDN();
        try {
            DirContext ctx = new InitialDirContext(env);

            ctx.createSubcontext(entryDN, getUserAttributes(ctx, user));
            logger.info("LDAP user added successfully: {}", user.getUsername());

            ctx.close();
        } catch (NamingException e) {
            if (e.getMessage().contains("Entry Already Exists")) {
                logger.debug("User {} already registered in LDAP", user.getUsername());
                throw new ResourceAlreadyExistsException("User already registered in LDAP");
            }
            logger.error("Error adding LDAP user: {}\n{} ", user.getUsername(), e.getMessage());
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
                    ctx.search(ldapConfig.getBaseDN(), searchFilter, searchControls);

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
            logger.error("Error searching uids: {}", e.getMessage());
        }
        return Optional.empty();
    }

}
