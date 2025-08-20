package com.cyverse.api.exceptions;

public class ApiTokenExpiredException extends Exception {
    public ApiTokenExpiredException() {
        super("Token expired exception");
    }
}
