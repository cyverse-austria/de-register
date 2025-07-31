import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public class Application {

    public static String TEST_REALM = "testrealm";

    public static void main(String[] args) {
        try (Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl("http://localhost:8087")
                .realm("master")
                .clientId("admin-cli")
                .username("admin")
                .password("admin")
                .build()) {

            System.out.println("Creating new realm: testrealm");
            RealmRepresentation rr = new RealmRepresentation();
            rr.setId(TEST_REALM);
            rr.setRealm(TEST_REALM);
            rr.setEnabled(true);

            keycloak.realms().create(rr);
            System.out.println("Realm creation: done");

            System.out.println("Adding new client: " + args[0]);
            String name = args[0];
            String port = args[1];
            String secret = args[2];

            keycloak.realm(TEST_REALM).clients().create(getClient(name, port, secret));
            System.out.println("Client add: done");

            System.out.println("Adding test users");
            keycloak.realm(TEST_REALM).users().create(getTestUser("test_user1"));
            keycloak.realm(TEST_REALM).users().create(getTestUser("test_user2"));
            System.out.println("Testing users: done");
        }
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
}
