package cz.cdcargo.javascaffold.shell.ui.webapp.platform.password;

import java.util.Arrays;
import java.util.Base64;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

public final class Argon2PasswordSupport {

    private static final int MAX_MEMORY_KIB = 1_048_576;
    private static final int MAX_ITERATIONS = 100;
    private static final int MAX_PARALLELISM = 32;

    private Argon2PasswordSupport() {
    }

    static byte[] derive(String password, byte[] salt, int memoryKiB, int iterations, int parallelism, int hashLength) {
        var parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withSalt(salt)
                .withMemoryAsKB(memoryKiB)
                .withIterations(iterations)
                .withParallelism(parallelism)
                .build();
        var passwordChars = password.toCharArray();
        var result = new byte[hashLength];

        try {
            var generator = new Argon2BytesGenerator();
            generator.init(parameters);
            generator.generateBytes(passwordChars, result);
            return result;
        } finally {
            Arrays.fill(passwordChars, '\0');
            parameters.clear();
        }
    }

    static String encode(byte[] salt, byte[] hash, int memoryKiB, int iterations, int parallelism) {
        var encoder = Base64.getEncoder().withoutPadding();
        return "$argon2id"
                + "$v=19"
                + "$m=" + memoryKiB
                + ",t=" + iterations
                + ",p=" + parallelism
                + "$" + encoder.encodeToString(salt)
                + "$" + encoder.encodeToString(hash);
    }

    static EncodedHash parse(String encoded) {
        var parts = encoded.split("\\$", -1);
        if (parts.length != 6 || !parts[0].isEmpty() || !"argon2id".equals(parts[1])) {
            throw new IllegalArgumentException("Unsupported password hash format");
        }
        var version = parseValue(parts[2], "v");
        if (version != 19) {
            throw new IllegalArgumentException("Unsupported Argon2 version");
        }
        var parameters = parts[3].split(",", -1);
        if (parameters.length != 3) {
            throw new IllegalArgumentException("Invalid Argon2 parameters");
        }

        var memoryKiB = parseValue(parameters[0], "m");
        var iterations = parseValue(parameters[1], "t");
        var parallelism = parseValue(parameters[2], "p");
        validateParameters(memoryKiB, iterations, parallelism);

        try {
            var decoder = Base64.getDecoder();
            var salt = decoder.decode(parts[4]);
            var hash = decoder.decode(parts[5]);

            if (salt.length < 8 || salt.length > 64) {
                throw new IllegalArgumentException("Invalid Argon2 salt length");
            }
            if (hash.length < 16 || hash.length > 64) {
                throw new IllegalArgumentException("Invalid Argon2 hash length");
            }
            return new EncodedHash(memoryKiB, iterations, parallelism, salt, hash);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Argon2 hash", e);
        }
    }

    static void validateParameters(int memoryKiB, int iterations, int parallelism) {
        if (memoryKiB <= 0 || memoryKiB > MAX_MEMORY_KIB) {
            throw new IllegalArgumentException("Invalid Argon2 memory cost");
        }
        if (iterations <= 0 || iterations > MAX_ITERATIONS) {
            throw new IllegalArgumentException("Invalid Argon2 iteration count");
        }
        if (parallelism <= 0 || parallelism > MAX_PARALLELISM) {
            throw new IllegalArgumentException("Invalid Argon2 parallelism");
        }
    }

    private static int parseValue(String value, String name) {
        var prefix = name + "=";
        if (!value.startsWith(prefix)) {
            throw new IllegalArgumentException("Missing Argon2 parameter: " + name);
        }

        try {
            return Integer.parseInt(value.substring(prefix.length()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Argon2 parameter: " + name, e);
        }
    }

    record EncodedHash(int memoryKiB, int iterations, int parallelism, byte[] salt, byte[] hash) {

        void clear() {
            Arrays.fill(salt, (byte) 0);
            Arrays.fill(hash, (byte) 0);
        }
    }

}
