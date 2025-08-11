import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Application {

    public static class Config {
        public String kcUrl;
        public String ldapUrl;
        public String ldapBindDn;
        public String ldapAdminPasswd;
        public String ldapUsersDn;
    }

    public static String TEST_REALM = "testrealm";

    public static void main(String[] args) {
        Config appConfig = new Config();
        try {
            appConfig = loadConfig(args[0]);
        } catch (Exception e) {
            System.out.println("Failed loading config. " + e.getMessage());
            System.exit(1);
        }

        try (Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(appConfig.kcUrl)
                .realm("master")
                .clientId("admin-cli")
                .username("admin")
                .password("admin")
                .build()) {

            // --------- Realm --------
            System.out.println("Creating new realm: testrealm");
            RealmRepresentation rr = new RealmRepresentation();
            rr.setId(TEST_REALM);
            rr.setRealm(TEST_REALM);
            rr.setEnabled(true);

            keycloak.realms().create(rr);
            System.out.println("Realm creation: done");

            // ---------- Client ---------
            System.out.println("Adding new client: " + args[1]);
            String name = args[1];
            String port = args[2];
            String secret = args[3];

            Response clientResp =
                    keycloak.realm(TEST_REALM)
                            .clients()
                            .create(getClient(name, port, secret));
            logResponse(clientResp);
            System.out.println("Client add: done");

            // ---------- Users ------------
            System.out.println("Adding test users");
            keycloak.realm(TEST_REALM).users().create(getTestUser("test_user1"));
            keycloak.realm(TEST_REALM).users().create(getTestUser("test_user2"));
            System.out.println("Testing users: done");

            // ---------- LDAP --------------
            System.out.println("Adding LDAP Federation");
            RealmRepresentation repr = keycloak.realm(TEST_REALM).toRepresentation();
            Response ldapResp = keycloak.realm(TEST_REALM)
                    .components()
                    .add(getLdapComponent(
                            repr.getId(),
                            appConfig));
            logResponse(ldapResp);
            System.out.println("LDAP Federation: done");

            System.out.println("Update LDAP mappers");
            List<ComponentRepresentation> ldapProviders = keycloak.realm(TEST_REALM)
                    .components()
                    .query("testrealm", "org.keycloak.storage.UserStorageProvider", null);


            ComponentRepresentation ldapProvider = ldapProviders.get(0);
            String ldapProviderId = ldapProvider.getId();

            List<ComponentRepresentation> mappers = keycloak.realm(TEST_REALM)
                    .components()
                    .query(ldapProviderId, "org.keycloak.storage.ldap.mappers.LDAPStorageMapper", null);
            List<ComponentRepresentation> updatedMappers = updateLDAPMappers(mappers);

            for (ComponentRepresentation mapper : updatedMappers) {
                System.out.println("Updated " + mapper.getName() + " mapper");
                keycloak.realm(TEST_REALM).components().component(mapper.getId()).update(mapper);
            }
            System.out.println("Update LDAP mappers done");
        }
    }

    private static List<ComponentRepresentation> updateLDAPMappers(List<ComponentRepresentation> mappers) {
        List<ComponentRepresentation> updatedMappers = new ArrayList<>();
        for (ComponentRepresentation mapper : mappers) {
            if ("first name".equalsIgnoreCase(mapper.getName())
                    || "last name".equalsIgnoreCase(mapper.getName())) {
                mapper.getConfig().putSingle("read.only", "false");
                mapper.getConfig().putSingle("always.read.value.from.ldap", "false");
                updatedMappers.add(mapper);
            }
        }
        return updatedMappers;
    }

    private static void logResponse(Response resp) {
        System.out.println("Request status: " + resp.getStatus());
        if (resp.getStatus() != HttpStatus.SC_CREATED) {
            System.out.println("Got HTTP Exception from Keycloak server:");
            System.out.println(resp.readEntity(String.class));
        }
    }

    private static ComponentRepresentation getLdapComponent(String parentId, Config config) {

        ComponentRepresentation ldapComponent = new ComponentRepresentation();
        ldapComponent.setName("ldap");
        ldapComponent.setProviderId("ldap");
        ldapComponent.setProviderType("org.keycloak.storage.UserStorageProvider");
        ldapComponent.setParentId(parentId);

        MultivaluedHashMap<String, String> ldapParams = new MultivaluedHashMap<>();
        ldapParams.put("enabled", List.of("true"));
        ldapParams.put("priority", List.of("0"));
        ldapParams.put("editMode", List.of("WRITABLE"));
        ldapParams.put("importEnabled", List.of("true"));
        ldapParams.put("syncRegistrations", List.of("true"));
        ldapParams.put("vendor", List.of("other"));
        ldapParams.put("usernameLDAPAttribute", List.of("uid"));
        ldapParams.put("rdnLDAPAttribute", List.of("uid"));
        ldapParams.put("uuidLDAPAttribute", List.of("entryUUID"));
        ldapParams.put("userObjectClasses", List.of("inetOrgPerson, organizationalPerson"));
        ldapParams.put("connectionUrl", List.of(config.ldapUrl));
        ldapParams.put("usersDn", List.of(config.ldapUsersDn));
        ldapParams.put("bindDn", List.of(config.ldapBindDn));
        ldapParams.put("bindCredential", List.of(config.ldapAdminPasswd));
        ldapParams.put("searchScope", List.of("1"));
        ldapParams.put("useTruststoreSpi", List.of("ldapsOnly"));

        ldapComponent.setConfig(ldapParams);

        return ldapComponent;
    }


    private static ClientRepresentation getClient(String name, String port, String secret) {
        ClientRepresentation client = new ClientRepresentation();
        client.setClientId(name);
        String baseURL = "http://127.0.0.1:" + port;
        client.setRootUrl(baseURL);
        client.setAdminUrl(baseURL);
        client.setWebOrigins(List.of(baseURL));
        client.setRedirectUris(List.of(baseURL + "/*"));
        client.setSecret(secret);
        client.setProtocol("openid-connect");
        client.setStandardFlowEnabled(true);
        client.setEnabled(true);

        return client;
    }

    private static UserRepresentation getTestUser(String username) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue("123456");
        credential.setTemporary(false);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setFirstName(username);
        user.setLastName("test_firstname");
        user.setEmail(username + "@gmail.com");
        user.setCredentials(List.of(credential));
        user.setEnabled(true);

        return user;
    }

    private static Config loadConfig(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(filePath), Config.class);
    }
}
