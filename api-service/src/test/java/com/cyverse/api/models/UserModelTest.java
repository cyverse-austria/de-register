package com.cyverse.api.models;

import com.cyverse.api.exceptions.UserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserModelTest {

    @Test
    void testValidUsernames() throws UserException {
        UserModel user = new UserModel();
        user.setUsername("test_user1");
        Assertions.assertDoesNotThrow(user::validateUsername);

        user.setUsername("cd ../");
        Assertions.assertThrows(UserException.class, user::validateUsername);

        user.setUsername("user && rm -rf /");
        Assertions.assertThrows(UserException.class, user::validateUsername);

        user.setUsername("testUser-2");
        Assertions.assertDoesNotThrow(user::validateUsername);
    }
}
