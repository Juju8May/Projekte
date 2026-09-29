package com.lisa.api.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PasswordHasherTest {
    
    @Test
    void createsPasswordHashAndSaltSuccessfully() {
        PasswordHasher hasher = new PasswordHasher();
        String password = "myPassword";
        PasswordHasher.Hash hash = hasher.create(password);

        assertNotNull(hash);
        assertNotNull(hash.hash());
        assertNotNull(hash.salt());
    }

    @Test 
    void matchesReturnsTrueForCorrectPassword() {
        PasswordHasher hasher = new PasswordHasher();
        String password = "myPassword";
        PasswordHasher.Hash hash = hasher.create(password);

        boolean result = hasher.matches(password, hash.salt(), hash.hash());
        assertEquals(true, result);
    }

    @Test
    void matchesReturnsFalseForIncorrectPassword() {
        // arrange
        PasswordHasher hasher = new PasswordHasher();
        String password = "myPassword";
        PasswordHasher.Hash hash = hasher.create(password);
        // act
        boolean result = hasher.matches("wrongPassword", hash.salt(), hash.hash());
        // assert 
        assertEquals(false, result);
    }

    @Test
    void matchesReturnsFalseForIncorrectSalt() {
        // arrange
        PasswordHasher hasher = new PasswordHasher();
        String password = "myPassword";
        PasswordHasher.Hash hash = hasher.create(password);
        // act
        boolean result = hasher.matches(password, "wrongSalt", hash.hash());
        // assert
        assertEquals(false, result);
    }
}
