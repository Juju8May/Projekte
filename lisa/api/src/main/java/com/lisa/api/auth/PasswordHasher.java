package com.lisa.api.auth;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

@Component
public class PasswordHasher {
    private static final int SALT_BYTES = 32;
    private static final int ITERATIONS = 310_000;
    private static final int KEY_BITS = 256;
    private final SecureRandom secureRandom = new SecureRandom();

    public Hash create(String password) {
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        return new Hash(hex(salt), hash(password, salt));
    }

    public boolean matches(String password, String saltHex, String expectedHash) {
        byte[] salt = HexFormat.of().parseHex(saltHex);
        return MessageDigest.isEqual(hash(password, salt).getBytes(StandardCharsets.UTF_8), expectedHash.getBytes(StandardCharsets.UTF_8));
    }

    private String hash(String password, byte[] salt) {
        try {
            PBEKeySpec specification = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS);
            byte[] derived = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(specification).getEncoded();
            specification.clearPassword();
            return hex(derived);
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 password hashing is unavailable", exception);
        }
    }

    private String hex(byte[] value) {
        return HexFormat.of().formatHex(value);
    }

    public record Hash(String salt, String hash) { }
}
