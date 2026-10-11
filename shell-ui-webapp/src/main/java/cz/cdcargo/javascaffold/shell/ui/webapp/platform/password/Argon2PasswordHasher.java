package cz.cdcargo.javascaffold.shell.ui.webapp.platform.password;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

public final class Argon2PasswordHasher implements PasswordHasher {

    public static final int DEFAULT_MEMORY_KIB = 19_456;
    public static final int DEFAULT_ITERATIONS = 2;
    public static final int DEFAULT_PARALLELISM = 1;

    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;

    private final SecureRandom secureRandom;
    private final int memoryKiB;
    private final int iterations;
    private final int parallelism;

    public Argon2PasswordHasher() {
        this(new SecureRandom(), DEFAULT_MEMORY_KIB, DEFAULT_ITERATIONS, DEFAULT_PARALLELISM);
    }

    public Argon2PasswordHasher(int memoryKiB, int iterations, int parallelism) {
        this(new SecureRandom(), memoryKiB, iterations, parallelism);
    }

    private Argon2PasswordHasher(SecureRandom secureRandom, int memoryKiB, int iterations, int parallelism) {
        this.secureRandom = Objects.requireNonNull(secureRandom, "Secure random must not be null");
        Argon2PasswordSupport.validateParameters(memoryKiB, iterations, parallelism);
        this.memoryKiB = memoryKiB;
        this.iterations = iterations;
        this.parallelism = parallelism;
    }

    @Override
    public String hash(String password) {
        Objects.requireNonNull(password, "Password must not be null");

        if (password.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        var salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        var hash = Argon2PasswordSupport.derive(password, salt, memoryKiB, iterations, parallelism, HASH_LENGTH);

        try {
            return Argon2PasswordSupport.encode(salt, hash, memoryKiB, iterations, parallelism);
        } finally {
            Arrays.fill(salt, (byte) 0);
            Arrays.fill(hash, (byte) 0);
        }
    }

}
