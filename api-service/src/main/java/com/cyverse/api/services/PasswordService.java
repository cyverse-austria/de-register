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
            // we only have 2 services for now that depend on this password (ldap, irods)
            // if both password are set, no need to keep it in memory
            // in this case - both are set means this method was called 2 times for the
            // same username
            String passwd = currentUsers.get(username);
            currentUsers.remove(username);
            return passwd;
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
