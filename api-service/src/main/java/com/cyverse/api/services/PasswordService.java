package com.cyverse.api.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PasswordService {
    private final Map<String, String> currentUsers;

    public PasswordService() {
        this.currentUsers = new HashMap<>();
    }

    /**
     * Generate a random user specific password and return it.
     */
    public String getGeneratedPassword(String username) {
        if (currentUsers.containsKey(username)) {
            return currentUsers.get(username);
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

        this.currentUsers.put(username, generatedPassword);
        return generatedPassword;
    }
}
