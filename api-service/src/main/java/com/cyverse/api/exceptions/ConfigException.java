package com.cyverse.api.exceptions;

/**
 * Custom exception for config related errors.
 */
public class ConfigException extends Exception {
    public ConfigException(String message) {
        super(message);
    }
}
