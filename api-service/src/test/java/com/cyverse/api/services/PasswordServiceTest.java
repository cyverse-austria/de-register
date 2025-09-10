package com.cyverse.api.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PasswordServiceTest {

    @Test
    void testSamePassword() {
        PasswordService passwordService = new PasswordService();
        String firstPass = passwordService.generatePasswordAndGet("test");
        String secondPass = passwordService.generatePasswordAndGet("test");

        Assertions.assertEquals(firstPass, secondPass);
    }
}
