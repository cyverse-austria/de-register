package com.cyverse.api.exceptions;

/**
 * Custom exception for an entity already present in LDAP or iRODS.
 */
public class ResourceAlreadyExistsException extends Exception {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
