package com.cyverse.api.models;

import com.cyverse.api.exceptions.UserException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class UserModel {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String group;

    @JsonIgnore
    public void validateUsername() throws UserException {
        String userPattern = "^[a-zA-Z0-9_-]{3,20}$";
        if (username == null || username.isEmpty() || !username.matches(userPattern)) {
            throw new UserException("Username not permitted");
        }
    }
}
