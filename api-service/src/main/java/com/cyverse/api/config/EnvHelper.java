package com.cyverse.api.config;

import java.util.Map;

public class EnvHelper {
    public String getEnv(String envName) {
        return System.getenv(envName);
    }

    public Map<String, String> getAllEnv() {
        return System.getenv();
    }
}
