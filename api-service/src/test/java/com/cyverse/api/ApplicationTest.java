package com.cyverse.api;

import com.cyverse.api.config.ApiServiceConfig;
import com.cyverse.api.config.EnvHelper;
import com.cyverse.api.exceptions.ConfigException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import static org.mockito.Mockito.doReturn;

public class ApplicationTest {

    @Mock
    EnvHelper envHelper;
    private static final String TEST_PREFIX = "TEST_";

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAppStart() {
        String configFile = "./app-test-file.yaml";
        String path = Objects.requireNonNull(getClass().getClassLoader().getResource(configFile)).getPath();

        Assertions.assertDoesNotThrow(() -> Application.main(new String[]{path}));
    }

    @Test
    void testAppGoodConfig() throws Exception {
        String configFile = "./app-test-file.yaml";
        String path = Objects.requireNonNull(getClass().getClassLoader().getResource(configFile)).getPath();
        ApiServiceConfig config = loadConfig(path, envHelper);
        Assertions.assertDoesNotThrow(config::verifyFieldsAreSet);
    }

    @Test
    void testAppWrongConfig() throws Exception {
        String configFile = "./app-wrong-file.yaml";
        String path = Objects.requireNonNull(getClass().getClassLoader().getResource(configFile)).getPath();
        ApiServiceConfig config = loadConfig(path, envHelper);
        Assertions.assertThrows(ConfigException.class, config::verifyFieldsAreSet);
    }

    @Test
    void testEnvConfig() throws Exception {
        // generic configs
        doReturn("7000").when(envHelper).getEnv(TEST_PREFIX+"PORT");

        // irods configs
        doReturn("test").when(envHelper).getEnv(TEST_PREFIX+"IRODS_PASSWORD");
        doReturn("TUG").when(envHelper).getEnv(TEST_PREFIX+"IRODS_ZONE");
        doReturn("false").when(envHelper).getEnv(TEST_PREFIX+"IRODS_IPC_SERVICES");

        // ldap configs
        doReturn("ldap://192.168.31.115:389/").when(envHelper).getEnv(TEST_PREFIX+"LDAP_HOST");
        doReturn("cn=Manager,dc=example,dc=org").when(envHelper).getEnv(TEST_PREFIX+"LDAP_ADMIN");
        doReturn("notreal").when(envHelper).getEnv(TEST_PREFIX+"LDAP_PASSWORD");
        doReturn("dc=example,dc=org").when(envHelper).getEnv(TEST_PREFIX+"LDAP_BASE_DN");
        doReturn("everyone").when(envHelper).getEnv(TEST_PREFIX+"LDAP_EVERYONE_GROUP");

        // portal configs
        doReturn("http://192.168.31.115:3000").when(envHelper).getEnv(TEST_PREFIX+"PORTAL_HOST");
        doReturn("<SECRET>").when(envHelper).getEnv(TEST_PREFIX+"PORTAL_HMAC_KEY");
        doReturn("7").when(envHelper).getEnv(TEST_PREFIX+"PORTAL_DIVISOR");

        // auth configs
        doReturn("<api_key>").when(envHelper).getEnv(TEST_PREFIX+"AUTH_API_KEY");
        doReturn("http://api-service.cyverse.at").when(envHelper).getEnv(TEST_PREFIX+"AUTH_TOKEN_ISSUER");
        doReturn(Map.of(TEST_PREFIX+"AUTH_USER_MAIL_test_user", "test.user@example.com",
                TEST_PREFIX+"AUTH_USER_PASSWORD_test_user", "testpass"))
                .when(envHelper).getAllEnv();
        doReturn("testpass").when(envHelper).getEnv(TEST_PREFIX+"AUTH_USER_PASSWORD_test_user");

        ApiServiceConfig configFromEnv = loadConfig("--from-env", envHelper);
        Assertions.assertDoesNotThrow(configFromEnv::verifyFieldsAreSet);

        String configFile = "./app-test-file.yaml";
        String path = Objects.requireNonNull(getClass().getClassLoader().getResource(configFile)).getPath();
        ApiServiceConfig configFromFile = loadConfig(path, envHelper);

        Assertions.assertEquals(configFromFile, configFromEnv);
    }

    private ApiServiceConfig loadConfig(String filePath, EnvHelper envHelper) throws Exception {
        if (filePath.equals("--from-env")) {
            return ApiServiceConfig.fromEnv(envHelper, TEST_PREFIX);
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(filePath), ApiServiceConfig.class);
    }
}
