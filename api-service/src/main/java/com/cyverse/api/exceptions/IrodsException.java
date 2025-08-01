package com.cyverse.api.exceptions;

/**
 * Custom exception for an error triggered by iRODS commands.
 */
public class IrodsException extends Exception {
    public IrodsException(String message) {
        super(message);
    }
}
