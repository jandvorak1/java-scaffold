package cz.cdcargo.javascaffold.shell.ui.webapp.platform.password;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;

public final class Argon2PasswordVerifier implements PasswordVerifier {

    @Override
    public boolean matches(String password, String passwordHash) {
        Objects.requireNonNull(password, "Password must not be null");
        Objects.requireNonNull(passwordHash, "Password hash must not be null");

        if (passwordHash.isBlank()) {
            return false;
        }
        Argon2PasswordSupport.EncodedHash encoded;
        try {
            encoded = Argon2PasswordSupport.parse(passwordHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }

        try {
            var actual = Argon2PasswordSupport.derive(password, encoded.salt(), encoded.memoryKiB(),
                    encoded.iterations(), encoded.parallelism(), encoded.hash().length);
            try {
                return MessageDigest.isEqual(actual, encoded.hash());
            } finally {
                Arrays.fill(actual, (byte) 0);
            }
        } finally {
            encoded.clear();
        }
    }
}
