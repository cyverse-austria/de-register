package com.cyverse.api;

import com.cyverse.api.config.ApiServiceConfig;
import com.cyverse.api.exceptions.ConfigException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Objects;

public class ApplicationTest {

    @Test
    void testAppStart() {
        String configFile = "./app-test-file.yaml";
        String path = Objects.requireNonNull(getClass().getClassLoader().getResource(configFile)).getPath();

        Assertions.assertDoesNotThrow(() -> Application.main(new String[]{path}));
    }

    @Test
    void testAppWrongConfig() throws Exception {
        String configFile = "./app-wrong-file.yaml";
        String path = Objects.requireNonNull(getClass().getClassLoader().getResource(configFile)).getPath();
        ApiServiceConfig config = loadConfig(path);
        Assertions.assertThrows(ConfigException.class, config::verifyFieldsAreSet);
    }

    private ApiServiceConfig loadConfig(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(filePath), ApiServiceConfig.class);
    }
}
