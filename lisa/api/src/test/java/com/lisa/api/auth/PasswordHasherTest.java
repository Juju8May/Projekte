package com.lisa.api.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PasswordHasherTest {
    private PasswordHasher hasher = new PasswordHasher();

    @Test
    void createGeneratesHashAndSalt() {
        String password = "testPassword";
        PasswordHasher.Hash hash = hasher.create(password);

        assertNotNull(hash.salt());
        assertNotNull(hash.hash());
        assertTrue(hash.salt().length() > 0);
        assertTrue(hash.hash().length() > 0);
    }

    @Test
    void matchesReturnsTrueForCorrectPassword() {
        String password = "testPassword";
        PasswordHasher.Hash hash = hasher.create(password);

        boolean result = hasher.matches(password, hash.salt(), hash.hash());

        assertTrue(result);
    }

    @Test
    void matchesReturnsFalseForIncorrectPassword() {
        String password = "testPassword";
        PasswordHasher.Hash hash = hasher.create(password);

        boolean result = hasher.matches("wrongPassword", hash.salt(), hash.hash());

        assertEquals(false, result);
    }
}