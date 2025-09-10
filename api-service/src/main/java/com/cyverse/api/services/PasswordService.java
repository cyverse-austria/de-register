package com.cyverse.api.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Password generator and storage for API users.
 */
public class PasswordService {

    private final Map<String, String> userToPasswordMapping;

    public PasswordService() {
        this.userToPasswordMapping = new HashMap<>();
    }

    /**
     * Generate a random user specific password and return it.
     */
    public String generatePasswordAndGet(String username) {
        if (userToPasswordMapping.containsKey(username)) {
            return userToPasswordMapping.get(username);
        }

        String randomSection = new Random().ints(
                10,
                        33,
                        122)
                .collect(StringBuilder::new,
                        StringBuilder::appendCodePoint,
                        StringBuilder::append)
                .toString();
        String generatedPassword = username.substring(0, 3) + randomSection;

        this.userToPasswordMapping.put(username, generatedPassword);
        return generatedPassword;
    }

    public String getPassword(String username) {
        return userToPasswordMapping.get(username);
    }
}
