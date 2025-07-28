package com.cyverse.api.exceptions;

/**
 * Custom exception for user related errors.
 */
public class UserException extends Exception {
    public UserException(String msg) {
        super(msg);
    }
}
